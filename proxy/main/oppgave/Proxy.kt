import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import oppgave.Config
import oppgave.OppgaveClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val SECURE_LOG: Logger = LoggerFactory.getLogger("secureLog")

fun main() {
    Thread.currentThread().setUncaughtExceptionHandler { _, e -> SECURE_LOG.error("Uhåndtert feil", e) }
    embeddedServer(Netty, port = 8080, module = Application::oppgaveProxy).start(wait = true)
}

fun Application.oppgaveProxy(
    config: Config = Config(),
) {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = prometheus
        meterBinders += LogbackMetrics()
    }

    install(ContentNegotiation) {
        jackson {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            registerModule(JavaTimeModule())
        }
    }

    val client = OppgaveClient(config)

    routing {
        actuators(prometheus)
        oppgave(client)
    }
}

private fun Route.oppgave(client: OppgaveClient) {
    route("/proxy/opprett") {
        post {
            val token = call.authToken()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            client.opprett(
                token = token,
                request = call.receive()
            )
        }
    }
}

private fun Routing.actuators(prometheus: PrometheusMeterRegistry) {
    route("/actuator") {
        get("/live") {
            call.respond(HttpStatusCode.OK, "live")
        }
        get("/ready") {
            call.respond(HttpStatusCode.OK, "live")
        }
        get("/metrics") {
            call.respond(HttpStatusCode.OK, prometheus.scrape())
        }
    }
}

internal fun ApplicationCall.authToken(): String? {
    return request.headers["Authorization"]
        ?.split(" ")
        ?.get(1)
}
