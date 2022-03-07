package no.nav.aap.app

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
import org.slf4j.LoggerFactory

private const val SØKERE_STORE_NAME = "oppgavestyring-soker-state-store"
private val secureLog = LoggerFactory.getLogger("secureLog")

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

internal fun Application.server(kafka: Kafka = KafkaSetup()) {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val config = loadConfig<Config>()

    install(MicrometerMetrics) { registry = prometheus }
    install(AapAuth) { providers += AzureADProvider(config.oauth.azure) }
    install(ContentNegotiation) { jackson { registerModule(JavaTimeModule()) } }

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

    val topics = Topics(config.kafka)
    val topology = createTopology(topics)
    kafka.start(topology, config.kafka)
    val søkerStore = kafka.getStore<Soker>(SØKERE_STORE_NAME)

    routing {
        actuator(prometheus)
        api(søkerStore, kafka, topics)
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

private fun Routing.api(søkerStore: ReadOnlyKeyValueStore<String, Soker>, kafka: Kafka, topics: Topics) {
    val manuellProducer = kafka.createProducer(topics.manuell)

    authenticate {
        route("/api") {
            get("/sak") {
                val søkere: List<Soker> = søkerStore.allValues()
                val saker = søkere.flatMap { it.toFrontendView() }
                call.respond(saker)
            }

            get("/sak/{personident}") {
                val personident = call.parameters.getOrFail("personident")
                val søkere = søkerStore.allValues()
                val saker = søkere.filter { it.personident == personident }.flatMap { it.toFrontendView() }
                call.respond(saker)
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

private fun createTopology(topics: Topics): Topology = StreamsBuilder().apply {
    val søkerKTable = stream(topics.søkere.name, topics.søkere.consumed("soker-consumed"))
        .logConsumed()
        .filter { _, value -> value != null }
        .peek { key, value -> secureLog.info("produced [$SØKERE_STORE_NAME] K:$key V:$value") }
        .toTable(named("sokere-as-ktable"), materialized<Soker>(SØKERE_STORE_NAME, topics.søkere))

    søkerKTable.scheduleCleanup(SØKERE_STORE_NAME) { record ->
        søkereToDelete.removeIf { it == record.value().personident }
    }
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
