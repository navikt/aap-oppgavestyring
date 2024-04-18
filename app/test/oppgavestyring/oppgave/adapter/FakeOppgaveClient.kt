package oppgavestyring.oppgave.adapter

import oppgavestyring.oppgave.OppgaveGateway
import oppgavestyring.oppgave.OppgaveId

class FakeOppgaveClient : OppgaveGateway {

    var map : MutableMap<OppgaveId, OpprettResponse> = HashMap()

    override suspend fun opprett(token: Token, request: OpprettRequest): Result<OpprettResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun endre(token: Token, oppgaveId: OppgaveId, request: PatchOppgaveRequest): Result<OpprettResponse> {
        val opprettResponse = OpprettResponse(
            id = oppgaveId.asLong(),
            versjon = request.versjon.toInt() + 1,
            tilordnetRessurs = request.tilordnetRessurs,
            tema = "AAP",
            oppgavetype = "JFR",
            status = Status.AAPNET,
            prioritet = Prioritet.NORM,
            aktivDato = "2024-04-25",
            tildeltEnhetsnr = "4863"
        )
        map.put(oppgaveId, opprettResponse)
        return Result.success(map.get(oppgaveId)!!)
    }

    override suspend fun hent(token: Token, oppgaveId: OppgaveId): Result<OpprettResponse> {
        return Result.success(map.get(oppgaveId)!!)
    }

    override suspend fun søk(token: Token, params: SøkQueryParams): Result<SøkOppgaverResponse> {
        TODO("Not yet implemented")
    }
}