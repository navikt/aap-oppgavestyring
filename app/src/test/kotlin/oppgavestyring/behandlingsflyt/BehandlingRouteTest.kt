package oppgavestyring.behandlingsflyt

import behandlingsflytRequest
import io.ktor.client.request.*
import io.ktor.http.*
import oppgavestyring.TestDatabase
import oppgavestyring.behandlingsflyt.dto.*
import oppgavestyring.config.db.DB_CONFIG_PREFIX
import oppgavestyring.config.db.Flyway
import oppgavestyring.oppgave.db.Oppgave
import oppgavestyring.oppgave.db.OppgaveTabell
import oppgavestyring.oppgavestyringWithFakes
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import java.time.LocalDateTime

@Nested
class BehandlingRouteTest {
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



    @Test
    fun `opprett oppgave`() {
        oppgavestyringWithFakes { _, client ->
            val actual = client.post("/behandling") {
                contentType(ContentType.Application.Json)
                setBody(
                    behandlingsflytRequest
                )
            }

            Assertions.assertEquals(HttpStatusCode.Created, actual.status)
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
                val nyOppgave = Oppgave.find {
                    (OppgaveTabell.saksnummer eq oppgave.saksnummer) and
                            (OppgaveTabell.avklaringbehovtype eq Avklaringsbehovtype.FATTE_VEDTAK) and
                            (OppgaveTabell.status eq Avklaringsbehovstatus.OPPRETTET)
                }

                org.assertj.core.api.Assertions.assertThat(oppgave.status).isEqualTo(Avklaringsbehovstatus.AVSLUTTET)
                org.assertj.core.api.Assertions.assertThat(nyOppgave).isNotNull
            }
        }


    }

}