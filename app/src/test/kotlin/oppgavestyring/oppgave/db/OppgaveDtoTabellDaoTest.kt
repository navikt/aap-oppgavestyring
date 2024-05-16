package oppgavestyring.oppgave.db

import oppgavestyring.TestDatabase
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.config.db.Flyway
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals


fun generateOppgave() = Oppgave.new {
    saksnummer = "23452345"
    behandlingsreferanse = "behandlingsreferanse"
    behandlingstype = Behandlingstype.Førstegangsbehandling
    status = Avklaringsbehovstatus.OPPRETTET
    avklaringsbehovOpprettetTidspunkt = LocalDateTime.now()
    behandlingOpprettetTidspunkt = LocalDateTime.now()
    personnummer = "12345678901"
    avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_SYKDOM
}


class OppgaveDtoTabellDaoTest {
    companion object {


        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            TestDatabase.start()
        }
    }

    @BeforeEach
    fun setup() {
        TestDatabase.reset()
        Flyway.migrate(TestDatabase.getConnection())
    }

    @Test
    fun `forventer at opprettete oppgaver får tildelt id og timestamp av databse`() {
        val opprettetOppgave = transaction {
            generateOppgave()
        }

        assertNotNull(opprettetOppgave.id)
        assertNotNull(opprettetOppgave.avklaringsbehovOpprettetTidspunkt)
    }

    @Test
    fun `forventer å kunne legge til utfører på oppgave`() {
        transaction {
            val opprettetOppgave = generateOppgave()

            Utfører.new {
                ident = UUID.randomUUID().toString()
                oppgave = opprettetOppgave
            }

            assertEquals(1, opprettetOppgave.utførere.count())
        }

    }

    @Test
    fun `forventer å kunne tildele oppgave til person`() {
        val ident = "YOLO"
        transaction {
            val opprettetOppgave = generateOppgave()

            Tildelt.new {
                this.ident = ident
                oppgave = opprettetOppgave
            }

            assertEquals(ident, opprettetOppgave.tildelt?.ident)
        }

    }

}