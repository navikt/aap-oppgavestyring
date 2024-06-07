package oppgavestyring.intern.oppgave.api

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import oppgavestyring.testutils.TestDatabase
import oppgavestyring.config.db.DB_CONFIG_PREFIX
import oppgavestyring.config.db.Flyway
import oppgavestyring.ekstern.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.ekstern.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.ekstern.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.intern.oppgave.NavIdent
import oppgavestyring.intern.oppgave.db.Oppgave
import oppgavestyring.intern.oppgave.db.Tildelt
import oppgavestyring.testutils.oppgavestyringWithFakes
import org.assertj.core.api.Assertions
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Nested
class OppgaveRouteTest {
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

            Assertions.assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes("oppgaveOpprettet")
                .isEqualTo(expected)
        }
    }

    @Test
    fun `hent alle oppgaver, verifiser at bare åpne oppgaver blir hentet`() {
        oppgavestyringWithFakes { fakes, client ->
            val åpenOppgaveId = transaction {
                genererOppgave().lukkOppgave()
                genererOppgave().id.value
            }

            val actual = client.get("/oppgaver") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
            }.body<OppgaverResponse>()

            Assertions.assertThat(actual.oppgaver.size)
                .isOne()

            Assertions.assertThat(actual.oppgaver.first().oppgaveId)
                .isEqualTo(åpenOppgaveId)
        }
    }

    @Test
    fun `hent alle oppgaver med sortering på $property`() {
        val sorteringsParameter = "?sortering=behandlingOpprettetTid=desc"

        oppgavestyringWithFakes { fakes, client ->
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

            Assertions.assertThat(actual.oppgaver.size)
                .isEqualTo(2)

            Assertions.assertThat(actual.oppgaver.first().oppgaveId)
                .isEqualTo(førsteOppgave)
        }
    }

    @Test
    fun `hent alle oppgaver med sortering på tildelt`() {
        val sorteringsParameter = "?sortering=tilordnetRessurs=desc"

        oppgavestyringWithFakes { fakes, client ->
            val førsteOppgave = transaction {

                val oppgave1 = genererOppgave()
                val oppgave2 = genererOppgave()
                Tildelt.new {
                    ident = NavIdent("K123456")
                    oppgave = oppgave1
                }
                Tildelt.new {
                    ident = NavIdent("A111111")
                    oppgave = oppgave2
                }

                oppgave1.id.value
            }

            val actual = client.get("/oppgaver$sorteringsParameter") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
            }.body<OppgaverResponse>()

            Assertions.assertThat(actual.oppgaver.size)
                .isEqualTo(2)

            Assertions.assertThat(actual.oppgaver.first().oppgaveId)
                .isEqualTo(førsteOppgave)
        }
    }

    @Test
    fun `hent alle oppgaver med filtrering på property`() {
        val sorteringsParameter = "?filtrering=avklaringsbehov=AVKLAR_SYKDOM"

        oppgavestyringWithFakes { fakes, client ->
            val førsteOppgave = transaction {
                genererOppgave().avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_BISTANDSBEHOV
                genererOppgave().id.value
            }

            val actual = client.get("/oppgaver$sorteringsParameter") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
            }.body<OppgaverResponse>()

            Assertions.assertThat(actual.oppgaver.size)
                .isEqualTo(1)

            Assertions.assertThat(actual.oppgaver.first().oppgaveId)
                .isEqualTo(førsteOppgave)
        }
    }

    @Test
    fun `hent alle oppgaver med filtrering på foedselsnummer`() {
        val sorteringsParameter = "?filtrering=foedselsnummer=101010"

        oppgavestyringWithFakes { fakes, client ->
            val førsteOppgave = transaction {
                val oppgave1 = genererOppgave()
                oppgave1.personnummer = "10101012345"
                genererOppgave()
                oppgave1.id.value
            }

            val actual = client.get("/oppgaver$sorteringsParameter") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
            }.body<OppgaverResponse>()

            Assertions.assertThat(actual.oppgaver.size)
                .isEqualTo(1)

            Assertions.assertThat(actual.oppgaver.first().oppgaveId)
                .isEqualTo(førsteOppgave)
        }
    }

    @Test
    fun `hent alle oppgaver med filtrering på relasjonsverdi`() {
        val sorteringsParameter = "?filtrering=tilordnetRessurs=K101010"

        oppgavestyringWithFakes { fakes, client ->
            val førsteOppgave = transaction {
                val oppgave1 = genererOppgave()
                Tildelt.new {
                    ident = NavIdent("K101010")
                    oppgave = oppgave1
                }
                genererOppgave()
                oppgave1.id.value
            }

            val actual = client.get("/oppgaver$sorteringsParameter") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
            }.body<OppgaverResponse>()

            Assertions.assertThat(actual.oppgaver.size)
                .isEqualTo(1)

            Assertions.assertThat(actual.oppgaver.first().oppgaveId)
                .isEqualTo(førsteOppgave)
        }
    }

    @Test
    fun `hent alle oppgaver med filtrering på enkelt dato`() {
        val filterTime = LocalDateTime.of(2020, 10, 10, 10, 10, 10, 11111)
        val urlTime = filterTime.truncatedTo(ChronoUnit.DAYS)

        val sorteringsParameter = "?filtrering=behandlingOpprettetTid%3D${urlTime.minusDays(3)}%2F${urlTime.plusDays(3)}"

        oppgavestyringWithFakes { fakes, client ->
            val førsteOppgave = transaction {
                genererOppgave()
                val oppgave = genererOppgave()
                oppgave.behandlingOpprettetTidspunkt = filterTime
                oppgave.id.value
            }

            val actual = client.get("/oppgaver$sorteringsParameter") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
            }.body<OppgaverResponse>()

            Assertions.assertThat(actual.oppgaver.size)
                .isEqualTo(1)

            Assertions.assertThat(actual.oppgaver.first().oppgaveId)
                .isEqualTo(førsteOppgave)
        }
    }

    @Test
    fun `hent alle oppgaver med filtrering på fra til dato`() {
        val filterTime = LocalDateTime.of(2020, 10, 10, 10, 10, 10, 11111)

        val sorteringsParameter = "?filtrering=behandlingOpprettetTid%3D${filterTime.truncatedTo(ChronoUnit.DAYS)}"

        oppgavestyringWithFakes { fakes, client ->
            val førsteOppgave = transaction {
                genererOppgave()
                val oppgave = genererOppgave()
                oppgave.behandlingOpprettetTidspunkt = filterTime
                oppgave.id.value
            }

            val actual = client.get("/oppgaver$sorteringsParameter") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
            }.body<OppgaverResponse>()

            Assertions.assertThat(actual.oppgaver.size)
                .isEqualTo(1)

            Assertions.assertThat(actual.oppgaver.first().oppgaveId)
                .isEqualTo(førsteOppgave)
        }
    }


    @Test
    fun `hent alle oppgaver med filtrering på flere verdier av samme property`() {
        val sorteringsParameter = "?filtrering=avklaringsbehov%3DFASTSETT_ARBEIDSEVNE%26avklaringsbehov%3DAVKLAR_BISTANDSBEHOV"

        oppgavestyringWithFakes { fakes, client ->
            transaction {
                genererOppgave().avklaringsbehovtype = Avklaringsbehovtype.FASTSETT_ARBEIDSEVNE
                genererOppgave().avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_BISTANDSBEHOV
                genererOppgave()
            }

            val actual = client.get("/oppgaver$sorteringsParameter") {
                accept(ContentType.Application.Json)
                contentType(ContentType.Application.Json)
            }.body<OppgaverResponse>()

            Assertions.assertThat(actual.oppgaver.size)
                .isEqualTo(2)

            Assertions.assertThat(actual.oppgaver.map { it.avklaringsbehov })
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
                contentType(ContentType.Application.Json)
                setBody(
                    TildelRessursRequest(
                        navIdent = "T123456",
                        versjon = 12
                    )
                )
            }

            Assertions.assertThat(actual.status)
                .isEqualTo(HttpStatusCode.NoContent)
        }
    }

    @Test
    fun `frigi oppgave fjerner tildeling fra oppgave`() {
        oppgavestyringWithFakes { fakes, client ->
            val oppgaveId = transaction {
                val oppgave = genererOppgave()
                Tildelt.new {
                    ident = NavIdent("T123456")
                    this.oppgave = oppgave
                }
                oppgave.id.value
            }

            client.patch("/oppgaver/$oppgaveId/frigi") {
            }

            transaction {
                Assertions.assertThat(Oppgave[oppgaveId].tildelt).isNull()
            }
        }
    }

    @Test
    fun `hent neste oppgave`() {
        oppgavestyringWithFakes { fakes, client ->
            val oppgaveId = transaction {
                val tildeltOppgave = genererOppgave()
                Tildelt.new {
                    ident = NavIdent("T123456")
                    this.oppgave = tildeltOppgave
                }
                genererOppgave().status = Avklaringsbehovstatus.AVSLUTTET
                genererOppgave().id.value
            }

            val actual = client.get("/oppgaver/nesteOppgave") {
                accept(ContentType.Application.Json)
            }.body<OppgaveDto>()

            Assertions.assertThat(actual).isNotNull()
            Assertions.assertThat(actual.oppgaveId).isEqualTo(oppgaveId)
            Assertions.assertThat(actual.tilordnetRessurs).isNotNull()
        }
    }
}