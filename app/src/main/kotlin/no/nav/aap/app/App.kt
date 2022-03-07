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
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.aap.app.config.Config
import no.nav.aap.app.config.loadConfig
import no.nav.aap.app.kafka.Kafka
import no.nav.aap.app.kafka.KafkaSetup
import no.nav.aap.app.kafka.Topics
import no.nav.aap.app.security.AapAuth
import no.nav.aap.app.security.AzureADProvider
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory

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
            if(uri.startsWith("/actuator")) return@intercept proceed()
            secureLog.info("Behandler kall til uri=$uri, metode=$metode")
            proceed()
            secureLog.info("Ferdig behandlet kall til uri=$uri, metode=$metode")
        } catch (e: Throwable) {
            secureLog.error(
                "Feil i behandling av kall til uri=$uri, metode=$metode", e
            )
            throw e
        }
    }

    val topics = Topics(config.kafka)
    kafka.start(config.kafka)

    routing {
        actuator(prometheus)
        api(kafka, topics)
    }
}

private fun Routing.actuator(prometheus: PrometheusMeterRegistry) {
    route("/actuator") {
        get("/metrics") { call.respond(prometheus.scrape()) }
        get("/live") { call.respond("oppgavestyring") }
        get("/ready") { call.respond("oppgavestyring") }
    }
}

private fun Routing.api(kafka: Kafka, topics: Topics) {
    val manuellProducer = kafka.createProducer(topics.manuell)

    authenticate {
        route("/api") {
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
