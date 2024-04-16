package oppgavestyring.oppgave

import kotlinx.coroutines.runBlocking
import oppgavestyring.oppgave.adapter.*
import oppgavestyring.oppgave.db.FakeOppgaveRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OppgaveServiceTest {

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

        val tilordnetRessurs = oppgaveGateway.map.get(oppgaveId.asLong())!!.tilordnetRessurs!!
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

class FakeOppgaveClient : OppgaveGateway {

    var map : MutableMap<Long, PatchOppgaveRequest> = HashMap()

    override suspend fun opprett(token: Token, request: OpprettRequest): Result<OpprettResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun endre(token: Token, request: PatchOppgaveRequest) {
        map.put(request.id, request)
    }

    override suspend fun hent(token: Token, oppgaveId: OppgaveId): Result<OpprettResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun søk(token: Token, params: SøkQueryParams): Result<SøkOppgaverResponse> {
        TODO("Not yet implemented")
    }
}
