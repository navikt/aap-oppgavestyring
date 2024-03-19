package oppgavestyring

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import oppgavestyring.proxy.OpprettResponse
import oppgavestyring.proxy.Prioritet
import oppgavestyring.proxy.Status
import java.time.LocalDate

class Fakes : AutoCloseable {
    val azure = embeddedServer(Netty, port = 0, module = Application::azure).apply { start() }
    val oppgave = embeddedServer(Netty, port = 0, module = Application::oppgave).apply { start() }

    val config = TestConfig(
        oppgavePort = oppgave.port(),
        azurePort = azure.port()
    )

    override fun close() {
        azure.stop(0, 0)
        oppgave.stop(0, 0)
    }
}

private fun Application.oppgave() {
    install(ContentNegotiation) { jackson {} }

    routing {
        post("/api/v1/oppgaver") {
            call.respond(
                HttpStatusCode.Created,
                OpprettResponse(
                    id = 123L,
                    tildeltEnhetsnr = "1234",
                    tema = "AAP",
                    oppgavetype = "BEH_SAK",
                    versjon = 1,
                    prioritet = Prioritet.NORM,
                    status = Status.OPPRETTET,
                    aktivDato = "${LocalDate.now()}",
                )
            )
        }
    }
}

private fun Application.azure() {
    install(ContentNegotiation) { jackson {} }

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

fun NettyApplicationEngine.port() =
    runBlocking { resolvedConnectors() }
        .first { it.type == ConnectorType.HTTP }
        .port

internal fun oppgavestyringWithFakes(
    test: suspend (client: HttpClient) -> Unit,
) {
    Fakes().use {
        testApplication {
            application { server(it.config) }

            val client = createClient {
                install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                    jackson {}
                }
            }

            test(client)
        }
    }
}
