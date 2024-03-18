import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import oppgave.Config
import oppgave.OppgaveClient

val SECURE_LOG: Logger = LoggerFactory.getLogger("secureLog")
val APP_LOG = LoggerFactory.getLogger("App")

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

    val client = OppgaveClient(config.oppgave)

    routing {
        actuators(prometheus)
        oppgave(client)
    }
}

private fun Route.oppgave(client: OppgaveClient) {
    route("/proxy/opprett") {
        post {
            val proxyRequest = call.receive<String>()
            client.opprett(proxyRequest)
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
