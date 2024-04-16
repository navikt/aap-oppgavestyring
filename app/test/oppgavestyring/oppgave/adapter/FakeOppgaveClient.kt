package oppgavestyring.oppgave.adapter

import oppgavestyring.oppgave.OppgaveGateway
import oppgavestyring.oppgave.OppgaveId

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