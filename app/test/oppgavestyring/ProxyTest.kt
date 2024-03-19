package oppgavestyring

import io.ktor.client.request.*
import io.ktor.http.*
import oppgavestyring.proxy.OpprettRequest
import oppgavestyring.proxy.Prioritet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ProxyTest {

    @Test
    fun `create oppgave`() {
        oppgavestyringWithFakes { client ->
            val actual = client.post("/oppgave/opprett") {
                contentType(ContentType.Application.Json)
                bearerAuth("token")
                accept(ContentType.Application.Json)
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
                        behandlingstema = null, // kommer fra jp som noen har satt for å få den tilbake
                        oppgavetype = "JFR",
                        behandlingstype = null, // kommer fra jp som noen har satt for å få den tilbake
                        aktivDato = "${LocalDate.now()}",
                        fristFerdigstillelse = null,
                        prioritet = Prioritet.NORM,
                    )
                )
            }

            assertEquals(HttpStatusCode.Created, actual.status)
        }
    }
}
