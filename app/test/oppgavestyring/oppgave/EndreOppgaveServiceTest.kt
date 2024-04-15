package oppgavestyring.oppgave

import kotlinx.coroutines.runBlocking
import oppgavestyring.oppgave.adapter.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EndreOppgaveServiceTest {

    private val fakeOppgaveClient = FakeOppgaveClient()
    private val endreOppgaveService = EndreOppgaveService(fakeOppgaveClient)

    @Test
    fun `skal_kunne_tildele_en_ressurs_til_en_oppgave`() {
        val oppgaveId = 1L

        runBlocking {
            endreOppgaveService.tildelRessursTilOppgave(
                oppgaveId,
                NavIdent("H113521"),
                Token("Dummy Token")
            )
        }

        val tilordnetRessurs = fakeOppgaveClient.map.get(oppgaveId)!!.tilordnetRessurs!!
        assertThat(tilordnetRessurs).isEqualTo("H113521")
    }

    @Test
    fun `skal_kunne_tildele_en_ressurs_til_en_oppgave2`() {
        val oppgaveId = 1L

        runBlocking {
            endreOppgaveService.tildelRessursTilOppgave(
                oppgaveId,
                NavIdent("H113521"),
                Token("Dummy Token")
            )
        }
    }
}

class FakeOppgaveClient : Oppgave {

    var map : MutableMap<Long, PatchOppgaveRequest> = HashMap()

    override suspend fun opprett(token: Token, request: OpprettRequest): Result<OpprettResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun endre(token: Token, request: PatchOppgaveRequest) {
        map.put(request.id, request)
    }

    override suspend fun hent(token: Token, oppgaveId: Long): Result<OpprettResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun søk(token: Token, params: SøkQueryParams): Result<SøkOppgaverResponse> {
        TODO("Not yet implemented")
    }
}
