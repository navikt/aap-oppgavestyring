package oppgavestyring.oppgave.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import oppgavestyring.TestDatabase
import oppgavestyring.behandlingsflyt.dto.*
import oppgavestyring.config.db.DB_CONFIG_PREFIX
import oppgavestyring.config.db.Flyway
import oppgavestyring.oppgave.db.Tildelt
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

            System.setProperty("${DB_CONFIG_PREFIX}_JDBC_URL", TestDatabase.connectionUrl)
            System.setProperty("${DB_CONFIG_PREFIX}_USERNAME", TestDatabase.username)
            System.setProperty("${DB_CONFIG_PREFIX}_PASSWORD", TestDatabase.password)
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
                    setBody("""{
                        "personident" : "14098929550",
                        "saksnummer" : "24352363",
                        "referanse" : "yolo",
                        "behandlingType" : "Klage",
                        "status" : "PÅ_VENT",
                        "avklaringsbehov" : [ 
                            {
                                "definisjon" : {
                                    "type" : "5003",
                                    "behovType" : "MANUELT_PÅKREVD",
                                    "løsesISteg" : "BARNETILLEGG"
                                },
                                "status" : "OPPRETTET",
                                "endringer" : [ {
                                    "status" : "OPPRETTET",
                                    "tidsstempel" : "2024-05-15T16:27:31.996299178",
                                    "frist" : "2025-05-15",
                                    "endretAv" : "T123456"
                                } ]
                            } ],
                        "opprettetTidspunkt" : "2024-05-15T16:27:31.985882524"
                    }"""
                    )
                }

                assertEquals(HttpStatusCode.Created, actual.status)
            }
        }

    }


    @Nested
    inner class OppgaveDto {

        @Test
        fun `hent oppgave`() {
            oppgavestyringWithFakes { fakes, client ->
                val avklaringsbehovTidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS)
                val behandlingTidspunkt = avklaringsbehovTidspunkt.minusDays(3)
                val personnummer = "12345678900"

                val oppgaveId = transaction {
                    oppgavestyring.oppgave.db.Oppgave.new {
                        behandlingsreferanse = "23642"
                        status = Avklaringsbehovstatus.OPPRETTET
                        avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_SYKDOM
                        behandlingstype = Behandlingstype.Førstegangsbehandling
                        avklaringsbehovOpprettetTidspunkt = avklaringsbehovTidspunkt
                        behandlingOpprettetTidspunkt = behandlingTidspunkt
                        this.personnummer = personnummer
                    }.id.value
                }

                val actual = client.get("/oppgaver/$oppgaveId") {
                    bearerAuth("token")
                    accept(ContentType.Application.Json)
                }.body<oppgavestyring.oppgave.api.OppgaveDto>()

                val expected = oppgavestyring.oppgave.api.OppgaveDto(
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

        @Test
        fun `tildelOppgave`() {
            oppgavestyringWithFakes { fakes, client ->
                val oppgaveId = transaction {
                    oppgavestyring.oppgave.db.Oppgave.new {
                        behandlingsreferanse = "23642"
                        status = Avklaringsbehovstatus.OPPRETTET
                        avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_SYKDOM
                        behandlingstype = Behandlingstype.Førstegangsbehandling
                        avklaringsbehovOpprettetTidspunkt = LocalDateTime.now()
                        behandlingOpprettetTidspunkt = LocalDateTime.now()
                        personnummer = "3564589"
                    }.id.value
                }

                val actual = client.patch("/oppgaver/$oppgaveId/tildelRessurs") {
                    bearerAuth("token")
                    contentType(ContentType.Application.Json)
                    setBody(
                        TildelRessursRequest(
                            navIdent = "T123456",
                            versjon = 12
                        )
                    )
                }

                assertEquals(HttpStatusCode.NoContent, actual.status)
            }
        }

        @Test
        fun `frigi oppgave`() {
            oppgavestyringWithFakes { fakes, client ->
                val oppgaveId = transaction {
                    val oppgave = oppgavestyring.oppgave.db.Oppgave.new {
                        behandlingsreferanse = "23642"
                        status = Avklaringsbehovstatus.OPPRETTET
                        avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_SYKDOM
                        behandlingstype = Behandlingstype.Førstegangsbehandling
                        avklaringsbehovOpprettetTidspunkt = LocalDateTime.now()
                        behandlingOpprettetTidspunkt = LocalDateTime.now()
                        personnummer = "3564589"
                    }
                    Tildelt.new {
                        ident = "T123456"
                        this.oppgave = oppgave
                    }
                    oppgave.id.value
                }

                val actual = client.patch("/oppgaver/$oppgaveId/frigi") {
                    bearerAuth("token")
                }

                assertEquals(HttpStatusCode.NoContent, actual.status)
            }
        }
    }
}
