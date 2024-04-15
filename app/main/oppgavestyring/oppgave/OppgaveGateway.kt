package oppgavestyring.oppgave

import oppgavestyring.oppgave.adapter.*

interface OppgaveGateway {
    suspend fun opprett(token: Token, request: OpprettRequest): Result<OpprettResponse>
    suspend fun endre(token: Token, request: PatchOppgaveRequest)
    suspend fun hent(token: Token, oppgaveId: Long): Result<OpprettResponse>
    suspend fun søk(token: Token, params: SøkQueryParams): Result<SøkOppgaverResponse>
}