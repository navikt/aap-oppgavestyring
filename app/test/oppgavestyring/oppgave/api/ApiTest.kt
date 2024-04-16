package oppgavestyring.oppgave.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import oppgavestyring.oppgave.adapter.OpprettRequest
import oppgavestyring.oppgave.adapter.OpprettResponse
import oppgavestyring.oppgave.adapter.Prioritet
import oppgavestyring.oppgave.adapter.Status
import oppgavestyring.oppgavestyringWithFakes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ApiTest {

    @Test
    fun `opprett oppgave`() {
        oppgavestyringWithFakes { _, client ->
            val actual = client.post("/oppgaver/opprett") {
                contentType(ContentType.Application.Json)
                bearerAuth("token")
                accept(ContentType.Application.Json)
                setBody(
                    OpprettRequest(
                        tema = "AAP",
                        oppgavetype = "JFR",
                        behandlingstema = null, // kommer fra jp som noen har satt for å få den tilbake
                        behandlingstype = null, // kommer fra jp som noen har satt for å få den tilbake
                        aktivDato = "${LocalDate.now()}",
                        prioritet = Prioritet.NORM,
                    )
                )
            }

            assertEquals(HttpStatusCode.Created, actual.status)
        }
    }

    @Test
    fun `hent oppgave`() {
        oppgavestyringWithFakes { fakes, client ->
            val now = LocalDate.now()

            val oppgaveId = client.post("/oppgaver/opprett") {
                contentType(ContentType.Application.Json)
                bearerAuth("token")
                accept(ContentType.Application.Json)
                setBody(
                    OpprettRequest(
                        tema = "AAP",
                        oppgavetype = "JFR",
                        behandlingstema = null, // kommer fra jp som noen har satt for å få den tilbake
                        behandlingstype = null, // kommer fra jp som noen har satt for å få den tilbake
                        aktivDato = "$now",
                        prioritet = Prioritet.NORM,
                    )
                )
            }.let {
                require(it.status == HttpStatusCode.Created) { "not created" }
                fakes.oppgave.oppgaveIdSeq
            }

            val actual = client.get("/oppgaver/$oppgaveId") {
                bearerAuth("token")
                accept(ContentType.Application.Json)
            }.body<OpprettResponse>()

            val expected = OpprettResponse(
                id = oppgaveId,
                tildeltEnhetsnr = "1234",
                tema = "AAP",
                oppgavetype = "JFR",
                versjon = 1,
                prioritet = Prioritet.NORM,
                status = Status.OPPRETTET,
                aktivDato = "$now",
            )

            assertEquals(expected, actual)
        }
    }
}
