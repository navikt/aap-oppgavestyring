package oppgavestyring.oppgave.db

import oppgavestyring.TestDatabase
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.behandlingsflyt.dto.Oppgavestatus
import oppgavestyring.config.db.Flyway
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals


fun generateOppgave() = Oppgave.new {
    behandlingsreferanse = UUID.randomUUID()
    behandlingstype = Behandlingstype.FØRSTEGANGSBEHANDLING
    status = Oppgavestatus.ÅPEN
    avklaringsbehovtype = Avklaringsbehovtype.AVKLAR_SYKDOM_KODE
}


class OppgaveTabellDaoTest {
    companion object {


        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            TestDatabase.start()
            Flyway.migrate(TestDatabase.getConnection())
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
        assertNotNull(opprettetOppgave.opprettet)
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

            assertEquals(ident, opprettetOppgave.tildelt.ident)
        }

    }

}