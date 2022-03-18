package no.nav.aap.app

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.ktor.util.collections.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.aap.app.config.Config
import no.nav.aap.app.config.loadConfig
import no.nav.aap.app.db.DbConfig
import no.nav.aap.app.frontendView.toFrontendView
import no.nav.aap.app.kafka.*
import no.nav.aap.app.security.AapAuth
import no.nav.aap.app.security.AzureADProvider
import no.nav.aap.avro.sokere.v1.Soker
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val secureLog = LoggerFactory.getLogger("secureLog")

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

internal fun Application.server(kafka: Kafka = KafkaSetup()) {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val config = loadConfig<Config>()

    install(MicrometerMetrics) { registry = prometheus }
    install(AapAuth) { providers += AzureADProvider(config.oauth.azure) }
    install(ContentNegotiation) { jackson {
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    } }

    intercept(ApplicationCallPipeline.Monitoring) {
        val uri = call.request.uri
        val metode = call.request.httpMethod.value
        try {
            if (uri.startsWith("/actuator")) return@intercept proceed()
            secureLog.info("Behandler kall til uri=$uri, metode=$metode")
            proceed()
            secureLog.info("Ferdig behandlet kall til uri=$uri, metode=$metode")
        } catch (e: Throwable) {
            secureLog.error("Feil i behandling av kall til uri=$uri, metode=$metode", e)
            throw e
        }
    }

    environment.monitor.subscribe(ApplicationStopping) { kafka.close() }

    val datasource = initDatasource(config.database)
    migrate(datasource)
    val repo = Repo(datasource)

    val topics = Topics(config.kafka)
    val topology = createTopology(repo, topics)
    kafka.start(topology, config.kafka)

    routing {
        actuator(prometheus)
        api(repo, kafka, topics)
        devTools(kafka, topics)
    }
}

private fun Routing.actuator(prometheus: PrometheusMeterRegistry) {
    route("/actuator") {
        get("/metrics") { call.respond(prometheus.scrape()) }
        get("/live") { call.respond("oppgavestyring") }
        get("/ready") { call.respond("oppgavestyring") }
    }
}

private fun Routing.api(repo: Repo, kafka: Kafka, topics: Topics) {
    val manuellProducer = kafka.createProducer(topics.manuell)

    authenticate {
        route("/api") {
            get("/sak") {
                call.respond(repo.hentSøkere())
            }

            get("/sak/{personident}") {
                val personident = call.parameters.getOrFail("personident")
                call.respond(repo.hentSøker(personident))
            }

            post("/sak/{personident}/losning") {
                val personident = call.parameters.getOrFail("personident")
                secureLog.info("Skal løse oppgave for $personident")
                val løsning = call.receive<DtoManuell>()
                withContext(Dispatchers.IO) {
                    manuellProducer.send(ProducerRecord(topics.manuell.name, personident, løsning.toAvro())).get()
                }
                call.respond(HttpStatusCode.OK, "OK")
            }
        }
    }
}

private fun createTopology(repo: Repo, topics: Topics): Topology = StreamsBuilder().apply {
    stream(topics.søkere.name, topics.søkere.consumed("soker-consumed"))
        .logConsumed()
        .filter { _, value -> value != null }
        .peek { key, value -> secureLog.info("produced K:$key V:$value") }
        .foreach { _, value -> repo.lagreSøker(value.toFrontendView()) }

}.build()

val søkereToDelete: ConcurrentList<String> = ConcurrentList()

private fun Routing.devTools(kafka: Kafka, topics: Topics) {
    val søkerProducer = kafka.createProducer(topics.søkere)

    fun <V> Producer<String, V>.tombstone(key: String) {
        send(ProducerRecord(topics.søkere.name, key, null)).get()
    }

    route("/delete/{personident}") {
        get {
            val personident = call.parameters.getOrFail("personident")
            søkerProducer.tombstone(personident).also {
                søkereToDelete.add(personident)
                secureLog.info("produced [${topics.søkere.name}] [$personident] [tombstone]")
            }
            call.respondText("Deleted $personident")
        }
    }
}

private fun initDatasource(dbConfig: DbConfig) = HikariDataSource(HikariConfig().apply {
    jdbcUrl = dbConfig.url
    username = dbConfig.username
    password = dbConfig.password
    maximumPoolSize = 3
    minimumIdle = 1
    initializationFailTimeout = 5000
    idleTimeout = 10001
    connectionTimeout = 1000
    maxLifetime = 30001
})

private fun migrate(dataSource: DataSource) {
    Flyway
        .configure()
        .cleanOnValidationError(true)
        .dataSource(dataSource)
        .load()
        .migrate()
}