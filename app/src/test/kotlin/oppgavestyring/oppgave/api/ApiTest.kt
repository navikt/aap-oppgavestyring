package oppgavestyring.oppgave.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import oppgavestyring.behandlingsflyt.Request
import oppgavestyring.oppgave.adapter.OpprettResponse
import oppgavestyring.oppgave.adapter.Prioritet
import oppgavestyring.oppgave.adapter.Status
import oppgavestyring.oppgavestyringWithFakes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class ApiTest {

    @Nested
    inner class `behandling`{

        @Test
        fun `opprett oppgave`() {
            oppgavestyringWithFakes { _, client ->
                val actual = client.post("/behandling") {
                    contentType(ContentType.Application.Json)
                    bearerAuth("token")
                    accept(ContentType.Application.Json)
                    setBody(
                        Request(
                            saksnummer = "4LDQPDS",
                            referanse = UUID.fromString("6ab21bd2-a036-40e5-bc14-cdc59b6ea4ce"),
                            personident = "12345678911",
                            //avklaringsbehov = "Dette er et avklaringsbehov",
                            status = oppgavestyring.behandlingsflyt.Status.OPPRETTET
                        )
                    )
                }

                assertEquals(HttpStatusCode.Created, actual.status)
            }
        }

        @Test
        fun `opprett oppgave med ekstra properties`() {
            data class TestRequest(
                val referanse: UUID,
                val saksnummer: String,
                val personident: String,
                val avklaringsbehov: String,
                val someOtherProp: Boolean,
                val status: oppgavestyring.behandlingsflyt.Status
                )

            oppgavestyringWithFakes { _, client ->
                val actual = client.post("/behandling") {
                    contentType(ContentType.Application.Json)
                    bearerAuth("token")
                    accept(ContentType.Application.Json)
                    setBody(
                        TestRequest(
                            saksnummer = "23452345",
                            personident = "12345678911",
                            avklaringsbehov = "Dette er et avklaringsbehov",
                            someOtherProp = false,
                            status = oppgavestyring.behandlingsflyt.Status.OPPRETTET,
                            referanse = UUID.randomUUID()
                        )
                    )
                }

                assertEquals(HttpStatusCode.Created, actual.status)
            }
        }
    }

    @Test
    fun `hent oppgave`() {
        oppgavestyringWithFakes { fakes, client ->
            val now = LocalDate.now()

            val oppgaveId = client.post("/behandling") {
                contentType(ContentType.Application.Json)
                bearerAuth("token")
                accept(ContentType.Application.Json)
                setBody(
                    Request(
                        saksnummer = "4LDQPDS",
                        referanse = UUID.fromString("6ab21bd2-a036-40e5-bc14-cdc59b6ea4ce"),
                        personident = "12345678911",
                        //avklaringsbehov = "Dette er et avklaringsbehov",
                        status = oppgavestyring.behandlingsflyt.Status.OPPRETTET
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
                oppgavetype = "BEH_SAK",
                versjon = 1,
                prioritet = Prioritet.NORM,
                status = Status.OPPRETTET,
                aktivDato = "$now",
            )

            assertEquals(expected, actual)
        }
    }
}
