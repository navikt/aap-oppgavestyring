package oppgavestyring.oppgave

import kotlinx.coroutines.runBlocking
import oppgavestyring.oppgave.adapter.*
import oppgavestyring.oppgave.db.FakeOppgaveRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OppgaveTabellServiceTest {

    private val oppgaveGateway = FakeOppgaveClient()
    private val oppgaveRepository = FakeOppgaveRepository
    private val endreOppgaveService = OppgaveService(oppgaveRepository, oppgaveGateway)

    @Test
    fun `skal_kunne_tildele_en_ressurs_til_en_oppgave`() {
        val oppgaveId = OppgaveId(1L)

        runBlocking {
            endreOppgaveService.tildelRessursTilOppgave(
                oppgaveId,
                Versjon(1L),
                NavIdent("H113521"),
                Token("Dummy Token")
            )
        }

        val response = oppgaveGateway.map.get(oppgaveId)
        val tilordnetRessurs = response!!.tilordnetRessurs
        assertThat(tilordnetRessurs).isEqualTo("H113521")
    }

    @Test
    fun `skal_kunne_tildele_en_ressurs_til_en_oppgave2`() {
        val oppgaveId = OppgaveId(1L)

        runBlocking {
            endreOppgaveService.tildelRessursTilOppgave(
                oppgaveId,
                Versjon(1L),
                NavIdent("H113521"),
                Token("Dummy Token")
            )
        }
    }
}

