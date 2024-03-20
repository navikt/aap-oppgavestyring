package oppgavestyring.fakes

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.port

class AzureFake : AutoCloseable {
    private val server = embeddedServer(Netty, port = 0, module = Application::azure).apply { start() }
    val port: Int get() = server.port()
    override fun close() = server.stop(0, 0)
}

private fun Application.azure() {
    install(ContentNegotiation) {
        jackson {}
    }

    routing {
        post("/token") {
            call.respondText(contentType = ContentType.Application.Json) {
                """
                    {
                        "exprires_in" : 3600,
                        "access_token": "very.secure.token"
                    }
                """.trimMargin()
            }
        }
    }
}