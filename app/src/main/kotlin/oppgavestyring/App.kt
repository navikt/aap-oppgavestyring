package oppgavestyring

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.response.OpenAPIPipelineResponseContext
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
import io.ktor.util.*
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import oppgavestyring.actuators.api.actuators
import oppgavestyring.config.db.DatabaseSingleton
import oppgavestyring.config.db.DbConfig
import oppgavestyring.config.security.AZURE
import oppgavestyring.config.security.authentication
import oppgavestyring.ekstern.behandlingsflyt.BehandlingsflytAdapter
import oppgavestyring.ekstern.behandlingsflyt.behandlingsflyt
import oppgavestyring.intern.filter.filter
import oppgavestyring.intern.oppgave.OppgaveService
import oppgavestyring.intern.oppgave.api.oppgaver
import org.jetbrains.exposed.dao.exceptions.EntityNotFoundException
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
            if (cause is IllegalArgumentException) {
                call.respondText(text = "400: $cause", status = HttpStatusCode.BadRequest)
            } else if (cause is EntityNotFoundException) {
                call.respondText(text = "404: $cause", status = HttpStatusCode.NotFound)
            } else {
                call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
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
            apiRoute {
                oppgaver(oppgaveService)
                filter()
            }
        }
        apiRoute {
            behandlingsflyt(behandlingsflytAdapter)
        }
    }
}

@KtorDsl
private fun Route.apiRoute(config: NormalOpenAPIRoute.() -> Unit) {
    NormalOpenAPIRoute(this, application.plugin(OpenAPIGen).globalModuleProvider).apply(config)
}

suspend inline fun <reified TResponse : Any> OpenAPIPipelineResponseContext<TResponse>.respondWithStatus(
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    response: TResponse
) {
    responder.respond(statusCode, response, pipeline)
}

suspend inline fun <reified TResponse : Any> OpenAPIPipelineResponseContext<TResponse>.respondWithStatus(
    statusCode: HttpStatusCode
) {
    responder.respond(statusCode, Unit, pipeline)
}

fun Application.server(
    config: Config = Config(),
) {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    install(MicrometerMetrics) {
        registry = prometheus
        meterBinders += LogbackMetrics()
    }

    install(OpenAPIGen) {
        // this serves OpenAPI definition on /openapi.json
        serveOpenApiJson = true
        // this servers Swagger UI on /swagger-ui/index.html
        serveSwaggerUi = true
        info {
            title = "AAP - Oppgavestyring"
        }
    }

    oppgavestyring(config)

    routing {
        actuators(prometheus)
    }
}
