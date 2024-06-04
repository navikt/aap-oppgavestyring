package oppgavestyring

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import oppgavestyring.actuators.api.actuators
import oppgavestyring.behandlingsflyt.BehandlingsflytAdapter
import oppgavestyring.behandlingsflyt.behandlingsflyt
import oppgavestyring.config.security.AZURE
import oppgavestyring.config.security.authentication
import oppgavestyring.config.db.DatabaseSingleton
import oppgavestyring.config.db.DbConfig
import oppgavestyring.filter.filter
import oppgavestyring.oppgave.OppgaveService
import oppgavestyring.oppgave.adapter.Token
import oppgavestyring.oppgave.api.oppgaver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val SECURE_LOG: Logger = LoggerFactory.getLogger("secureLog")
val LOG: Logger = LoggerFactory.getLogger("oppgavestyring")

fun main() {
    Thread.currentThread().setUncaughtExceptionHandler { _, e -> SECURE_LOG.error("Uhåndtert feil", e) }
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

fun Application.oppgavestyring(config: Config) {
    install(ContentNegotiation) {
        jackson {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
            registerModule(JavaTimeModule())
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            LOG.warn("Exception during request handling with cause: $cause")
            LOG.debug(cause.stackTrace.contentToString())
            if(cause is IllegalArgumentException) {
                call.respondText(text = "400: $cause" , status = HttpStatusCode.BadRequest)
            } else {
                call.respondText(text = "500: $cause" , status = HttpStatusCode.InternalServerError)
            }
        }
    }

    DatabaseSingleton.init(DbConfig())
    DatabaseSingleton.migrate()

    authentication(config.azure)

    val oppgaveService = OppgaveService()
    val behandlingsflytAdapter = BehandlingsflytAdapter(oppgaveService)

    routing {
        authenticate(AZURE) {
            oppgaver(oppgaveService)
            behandlingsflyt(behandlingsflytAdapter)
            filter()
        }

    }
}

fun Application.server(
    config: Config = Config(),
) {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = prometheus
        meterBinders += LogbackMetrics()
    }

    oppgavestyring(config)

    routing {
        actuators(prometheus)
    }
}

internal fun ApplicationCall.authToken(): Token? {
    return request.headers["Authorization"]
        ?.split(" ")
        ?.get(1)
        ?.let { Token(it) }
}
