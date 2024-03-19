package oppgavestyring

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import oppgavestyring.proxy.OpprettRequest
import oppgavestyring.proxy.Prioritet
import org.junit.jupiter.api.Test
import java.time.LocalDate
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation

class ProxyTest {

    @Test
    fun test() {
        val config = TestConfig()
        testApplication {
            application {
                server(config)
            }
            externalServices {
                hosts(config.oppgave.host.host) {
                    install(ContentNegotiation) {
                        jackson {}
                    }
                    routing {
                        post("/opprettoppgave") {
                            call.respondText("OK")
                        }
                    }
                }
                hosts(config.azure.tokenEndpoint) {
                    install(ContentNegotiation) {
                        jackson {}
                    }
                    routing {
                        post("/token") {
                            call.respond(TestToken())
                        }
                    }
                }
            }

            val client = createClient {
                install(ClientContentNegotiation) {
                    jackson {}
                }
            }


            val actual = client.post("proxy/opprett") {
                contentType(ContentType.Application.Json)
                bearerAuth("token")
                setBody(
                    OpprettRequest(
                        personident = null,
                        orgnr = null,
                        tildeltEnhetsnr = null,
                        opprettetAvEnhetsnr = null,
                        journalpostId = null,
                        behandlesAvApplikasjon = null,
                        tilordnetRessurs = null,
                        beskrivelse = null,
                        tema = "AAP",
                        behandlingstema = null,
                        oppgavetype = "JFR",
                        behandlingstype = null,
                        aktivDato = "${LocalDate.now()}",
                        fristFerdigstillelse = null,
                        prioritet = Prioritet.NORM,
                    )
                )
            }

//            assertEquals(HttpStatusCode.OK, actual.status)
//            assertEquals("Oppgave opprettet", actual.bodyAsText())
        }
    }
}

data class TestToken(
    val exprires_in: Int = 3599,
    val access_token: String = "very.secure.token"
)
