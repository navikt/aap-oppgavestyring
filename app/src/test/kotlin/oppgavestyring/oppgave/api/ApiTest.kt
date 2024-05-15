package oppgavestyring.oppgave.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import oppgavestyring.TestDatabase
import oppgavestyring.behandlingsflyt.dto.*
import oppgavestyring.config.db.Flyway
import oppgavestyring.oppgave.db.Oppgave
import oppgavestyring.oppgavestyringWithFakes
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class ApiTest {
    companion object {


        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            TestDatabase.start()
            Flyway.migrate(TestDatabase.getConnection())
        }
    }

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
                        BehandlingshistorikkRequest(
                            saksnummer = "4LDQPDS",
                            behandlingsreferanse = "6ab21bd2-a036-40e5-bc14-cdc59b6ea4ce",
                            personident = "12345678911",
                            behandlingType = Behandlingstype.FØRSTEGANGSBEHANDLING,
                            status = Behandlingstatus.OPPRETTET,
                            opprettetTidspunkt = LocalDateTime.now(),
                            avklaringsbehov = listOf(
                                AvklaringsbehovhendelseDto(
                                    definisjon = Definisjon("5003"),
                                    status = Avklaringsbehovstatus.OPPRETTET,
                                    endringer = listOf(
                                        AvklaringsbehovhendelseEndring(
                                            status = Avklaringsbehovstatus.OPPRETTET,
                                            tidsstempel = LocalDateTime.now(),
                                            endretAv = "23563247"
                                        )
                                    )
                                )
                            )
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
            val avklaringsbehovTidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
            val behandlingTidspunkt = avklaringsbehovTidspunkt.minusDays(3)
            val personnummer = "12345678900"

            val oppgaveId = transaction {
                Oppgave.new {
                    behandlingsreferanse = "23642"
                    status = Avklaringsbehovstatus.OPPRETTET
                    avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_SYKDOM
                    behandlingstype = Behandlingstype.FØRSTEGANGSBEHANDLING
                    avklaringsbehovOpprettetTidspunkt = avklaringsbehovTidspunkt
                    behandlingOpprettetTidspunkt = behandlingTidspunkt
                    this.personnummer = personnummer
                }.id.value
            }

            val actual = client.get("/oppgaver/$oppgaveId") {
                bearerAuth("token")
                accept(ContentType.Application.Json)
            }.body<oppgavestyring.oppgave.api.Oppgave>()

            val expected = oppgavestyring.oppgave.api.Oppgave(
                oppgaveId = oppgaveId,
                avklaringsbehov = Avklaringsbehovtype.AVKLAR_SYKDOM,
                status = Avklaringsbehovstatus.OPPRETTET,
                foedselsnummer = personnummer,
                avklaringsbehovOpprettetTid = avklaringsbehovTidspunkt,
                behandlingOpprettetTid = behandlingTidspunkt
            )

            assertEquals(expected, actual)
        }
    }
}
