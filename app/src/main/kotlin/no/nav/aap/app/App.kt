package no.nav.aap.app

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.aap.app.db.DbConfig
import no.nav.aap.app.frontendView.toFrontendView
import no.nav.aap.app.kafka.PersonopplysningerKafkaDto
import no.nav.aap.app.kafka.Topics
import no.nav.aap.kafka.KafkaConfig
import no.nav.aap.kafka.streams.*
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

private val secureLog = LoggerFactory.getLogger("secureLog")

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

internal fun Application.server(kafka: KStreams = KafkaStreams) {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val config = loadConfig<Config>()

    install(MicrometerMetrics) { registry = prometheus }
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    val jwkProvider: JwkProvider = JwkProviderBuilder(config.oauth.azure.jwksUrl)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        jwt {
            realm = "hent oppgaver"
            verifier(jwkProvider, config.oauth.azure.issuer)
            validate { cred -> JWTPrincipal(cred.payload) }
            challenge { _, _ -> call.respond(HttpStatusCode.Unauthorized, "not authed") }
        }
    }

    environment.monitor.subscribe(ApplicationStopping) { kafka.close() }

    val datasource = initDatasource(config.database)
    migrate(datasource)
    val repo = Repo(datasource)

    kafka.start(config.kafka, prometheus) {
        val søkerKStream = consume(Topics.søkere)

        consume(Topics.personopplysninger)
            .filterNotNull("filter-personopplysning-tombstones")
            .filterNot(erUfullstendig)
            .peek { key, value -> secureLog.info("saving [${Topics.personopplysninger}] K:$key V:$value") }
            .foreach { personident, value -> repo.lagrePersonopplysninger(value.toFrontendView(personident)) }

        val presentSøkereStream = søkerKStream.filterNotNull("filter-sokere-tombstone")

        presentSøkereStream
            .peek { key, value -> secureLog.info("saving [${Topics.søkere}] K:$key V:$value") }
            .foreach { _, value -> repo.lagreSøker(value.toFrontendView()) }

        presentSøkereStream
            .mapValues { _, _ -> PersonopplysningerKafkaDto() }
            .produce(Topics.personopplysninger, "produce-empty-personopplysninger")

        søkerKStream.filter { _, value -> value == null }
            .peek { key, value -> secureLog.info("deleted [${Topics.søkere}] K:$key V:$value") }
            .foreach { key, _ -> repo.slettSøker(key) }

        consume(Topics.mottakere)
            .filterNotNull("filter-mottakere-tombstone")
            .foreach { _, mottaker -> repo.lagreMottaker(mottaker.toFrontendView()) }
    }

    routing {
        actuator(prometheus, kafka)
        api(repo, config.kafka, kafka)
    }
}

private val erUfullstendig: (_: String, value: PersonopplysningerKafkaDto) -> Boolean = { k, v ->
    listOf(v.norgEnhetId, v.adressebeskyttelse, v.geografiskTilknytning, v.skjerming)
        .any { it == null }
        .also { if (it) secureLog.info("skipped [${Topics.personopplysninger}] K:$k V:$v") }
}

private fun Routing.actuator(prometheus: PrometheusMeterRegistry, kafka: KStreams) {
    route("/actuator") {
        get("/metrics") {
            call.respond(prometheus.scrape())
        }

        get("/live") {
            val status = if (kafka.isLive()) HttpStatusCode.OK else HttpStatusCode.InternalServerError
            call.respond(status, "oppgavestyring")
        }

        get("/ready") {
            val status = if (kafka.isReady()) HttpStatusCode.OK else HttpStatusCode.InternalServerError
            call.respond(status, "oppgavestyring")
        }
    }
}

private fun Routing.api(repo: Repo, config: KafkaConfig, kafka: KStreams) {
    val manuellProducer = kafka.createProducer(config, Topics.manuell)

    authenticate {
        route("/api") {
            get("/personopplysninger/{personident}") {
                val personident = call.parameters.getOrFail("personident")
                when (val opplysninger = repo.hentPersonopplysninger(personident)) {
                    null -> call.respond(HttpStatusCode.NotFound)
                    else -> call.respond(HttpStatusCode.OK, opplysninger)
                }
            }

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
                    manuellProducer.send(ProducerRecord(Topics.manuell.name, personident, løsning.toKafkaDto())).get()
                }
                call.respond(HttpStatusCode.OK, "OK")
            }
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
