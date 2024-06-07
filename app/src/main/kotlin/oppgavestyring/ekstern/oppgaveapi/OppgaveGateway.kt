package oppgavestyring.ekstern.oppgaveapi

import oppgavestyring.ekstern.oppgaveapi.adapter.*
import oppgavestyring.intern.oppgave.OppgaveId

interface OppgaveGateway {
    suspend fun opprett(token: Token, request: OpprettRequest): Result<OpprettResponse>
    suspend fun endre(token: Token, oppgaveId: OppgaveId, request: PatchOppgaveRequest): Result<OpprettResponse>
    suspend fun hent(token: Token, oppgaveId: OppgaveId): Result<OpprettResponse>
    suspend fun søk(token: Token, params: SøkQueryParams): Result<SøkOppgaverResponse>
}