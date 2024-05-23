package oppgavestyring.oppgave.api

import behandlingsflytRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import oppgavestyring.TestDatabase
import oppgavestyring.behandlingsflyt.dto.*
import oppgavestyring.config.db.DB_CONFIG_PREFIX
import oppgavestyring.config.db.Flyway
import oppgavestyring.oppgave.db.Oppgave
import oppgavestyring.oppgave.db.OppgaveTabell
import oppgavestyring.oppgave.db.Tildelt
import oppgavestyring.oppgavestyringWithFakes
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun setup() {
        TestDatabase.reset()
        Flyway.migrate(TestDatabase.getConnection())
    }

    @Nested
    inner class `behandling route` {

        @Test
        fun `opprett oppgave`() {
            oppgavestyringWithFakes { _, client ->
                val actual = client.post("/behandling") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        behandlingsflytRequest
                    )
                }

                assertEquals(HttpStatusCode.Created, actual.status)
            }
        }


        @Test
        fun `lukk oppgave på samme behandling`() {
            oppgavestyringWithFakes { _, client ->
                val oppgave = transaction {
                    Oppgave.new {
                        saksnummer = "2352345"
                        behandlingsreferanse = "354636"
                        personnummer = "12345432543"
                        status = Avklaringsbehovstatus.OPPRETTET
                        avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_SYKDOM
                        behandlingstype = Behandlingstype.Førstegangsbehandling
                        avklaringsbehovOpprettetTidspunkt = LocalDateTime.now()
                        behandlingOpprettetTidspunkt = LocalDateTime.now()
                        this.personnummer = personnummer
                    }
                }

                client.post("/behandling") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        BehandlingshistorikkRequest(
                            saksnummer = oppgave.saksnummer,
                            behandlingType = oppgave.behandlingstype,
                            status = Behandlingstatus.OPPRETTET,
                            personident = oppgave.personnummer,
                            avklaringsbehov = listOf(
                                AvklaringsbehovDto(
                                    definisjon = Definisjon(Avklaringsbehovtype.FATTE_VEDTAK.kode),
                                    status = Avklaringsbehovstatus.OPPRETTET,
                                    endringer = listOf(
                                        AvklaringsbehovhendelseEndring(
                                            status = Avklaringsbehovstatus.OPPRETTET,
                                            endretAv = "yolo",
                                            tidsstempel = LocalDateTime.now()
                                        )
                                    )
                                )
                            ),
                            opprettetTidspunkt = LocalDateTime.now(),
                            referanse = oppgave.behandlingsreferanse

                        )
                    )
                }

                transaction {
                    oppgave.refresh()
                    val nyOppgave = Oppgave.find { (OppgaveTabell.saksnummer eq oppgave.saksnummer) and
                            (OppgaveTabell.avklaringbehovtype eq Avklaringsbehovtype.FATTE_VEDTAK) and
                            (OppgaveTabell.status eq Avklaringsbehovstatus.OPPRETTET)
                    }

                    assertThat(oppgave.status).isEqualTo(Avklaringsbehovstatus.AVSLUTTET)
                    assertThat(nyOppgave).isNotNull
                }
            }


        }

    }


    @Nested
    inner class `oppgave route` {

        fun genererOppgave() = Oppgave.new {
            saksnummer = "2352345"
            behandlingsreferanse = "23642"
            personnummer = "12345432543"
            status = Avklaringsbehovstatus.OPPRETTET
            avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_SYKDOM
            behandlingstype = Behandlingstype.Førstegangsbehandling
            avklaringsbehovOpprettetTidspunkt =
                LocalDateTime.of(2020, 1, 1, 1, 1, 1, 222).truncatedTo(ChronoUnit.MILLIS)
            behandlingOpprettetTidspunkt = LocalDateTime.of(2021, 1, 1, 1, 1, 1, 222).truncatedTo(ChronoUnit.MILLIS)
            this.personnummer = personnummer
        }

        @Test
        fun `hent oppgave`() {
            oppgavestyringWithFakes { fakes, client ->

                val oppgave = transaction {
                    genererOppgave()
                }

                val actual = client.get("/oppgaver/${oppgave.id.value}") {
                    accept(ContentType.Application.Json)
                }.body<OppgaveDto>()

                val expected = OppgaveDto(
                    oppgaveId = oppgave.id.value,
                    saksnummer = "2352345",
                    behandlingsreferanse = "23642",
                    behandlingstype = Behandlingstype.Førstegangsbehandling,
                    avklaringsbehov = Avklaringsbehovtype.AVKLAR_SYKDOM,
                    status = Avklaringsbehovstatus.OPPRETTET,
                    foedselsnummer = oppgave.personnummer,
                    avklaringsbehovOpprettetTid = oppgave.avklaringsbehovOpprettetTidspunkt,
                    behandlingOpprettetTid = oppgave.behandlingOpprettetTidspunkt,
                    oppgaveOpprettet = LocalDateTime.now()
                )

                assertThat(actual)
                    .usingRecursiveComparison()
                    .ignoringFieldsMatchingRegexes("oppgaveOpprettet")
                    .isEqualTo(expected)
            }
        }

        @Test
        fun `hent alle oppgaver, verifiser at bare åpne oppgaver blir hentet`() {
            oppgavestyringWithFakes{fakes, client ->
                val åpenOppgaveId = transaction {
                    genererOppgave().lukkOppgave()
                    genererOppgave().id.value
                }

                val actual = client.get("/oppgaver") {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                }.body<OppgaverResponse>()

                assertThat(actual.oppgaver.size)
                    .isOne()

                assertThat(actual.oppgaver.first().oppgaveId)
                    .isEqualTo(åpenOppgaveId)
            }
        }

        @Test
        fun `hent alle oppgaver med sortering på $property`() {
            val sorteringsParameter = "?sortering=behandlingOpprettetTid=desc"

            oppgavestyringWithFakes{fakes, client ->
                val førsteOppgave = transaction {
                    genererOppgave().behandlingOpprettetTidspunkt = LocalDateTime.of(2020, 10, 1, 10, 10, 10)
                    val førsteOppgave = genererOppgave()
                        førsteOppgave.behandlingOpprettetTidspunkt = LocalDateTime.of(2020, 10, 1, 10, 10, 11)
                    førsteOppgave.id.value
                }

                val actual = client.get("/oppgaver$sorteringsParameter") {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                }.body<OppgaverResponse>()

                assertThat(actual.oppgaver.size)
                    .isEqualTo(2)

                assertThat(actual.oppgaver.first().oppgaveId)
                    .isEqualTo(førsteOppgave)
            }
        }

        @Test
        fun `hent alle oppgaver med filtrering på property`() {
            val sorteringsParameter = "?filtrering=avklaringsbehov=AVKLAR_SYKDOM"

            oppgavestyringWithFakes{fakes, client ->
                val førsteOppgave = transaction {
                    genererOppgave().avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_STUDENT
                    genererOppgave().id.value
                }

                val actual = client.get("/oppgaver$sorteringsParameter") {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                }.body<OppgaverResponse>()

                assertThat(actual.oppgaver.size)
                    .isEqualTo(1)

                assertThat(actual.oppgaver.first().oppgaveId)
                    .isEqualTo(førsteOppgave)
            }
        }

        @Test
        fun `hent alle oppgaver med filtrering på flere verdier av samme property`() {
            val sorteringsParameter = "?filtrering=avklaringsbehov%3DAVKLAR_STUDENT%26avklaringsbehov%3DAVKLAR_BISTANDSBEHOV"

            oppgavestyringWithFakes{fakes, client ->
                transaction {
                    genererOppgave().avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_STUDENT
                    genererOppgave().avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_BISTANDSBEHOV
                    genererOppgave()
                }

                val actual = client.get("/oppgaver$sorteringsParameter") {
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                }.body<OppgaverResponse>()

                assertThat(actual.oppgaver.size)
                    .isEqualTo(2)

                assertThat(actual.oppgaver.map { it.avklaringsbehov })
                    .doesNotContain(Avklaringsbehovtype.AVKLAR_SYKDOM)
            }
        }

        @Test
        fun `tildelOppgave`() {
            oppgavestyringWithFakes { fakes, client ->
                val oppgaveId = transaction {
                    genererOppgave().id.value
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

                assertThat(actual.status)
                    .isEqualTo(HttpStatusCode.OK)
            }
        }

        @Test
        fun `frigi oppgave fjerner tildeling fra oppgave`() {
            oppgavestyringWithFakes { fakes, client ->
                val oppgaveId = transaction {
                    val oppgave = genererOppgave()
                    Tildelt.new {
                        ident = "T123456"
                        this.oppgave = oppgave
                    }
                    oppgave.id.value
                }

                client.patch("/oppgaver/$oppgaveId/frigi") {
                    bearerAuth("token")
                }

                transaction {
                    assertThat(Oppgave[oppgaveId].tildelt).isNull()
                }
            }
        }
    }
}
