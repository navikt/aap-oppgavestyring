package oppgavestyring.oppgave

import oppgavestyring.oppgave.adapter.*

class OppgaveService(private val oppgaveRepository: OppgaveRepository, private val oppgaveGateway: OppgaveGateway) {

    suspend fun opprett(token: Token, request: OpprettRequest): Result<OpprettResponse> {
        val nyOppgave = oppgaveGateway.opprett(
            token = token,
            request = request
        )

        nyOppgave.onSuccess {
            oppgaveRepository.lagre(it)
        }

        return nyOppgave
    }

    suspend fun tildelRessursTilOppgave(id: OppgaveId, versjon: Versjon, navIdent: NavIdent, token: Token) {
        oppgaveGateway.endre(
            token = token,
            oppgaveId = id,
            request = PatchOppgaveRequest(
                versjon = versjon.asLong(),
                tilordnetRessurs = navIdent.asString()))
    }

    suspend fun frigiRessursFraOppgave(id: OppgaveId, versjon: Versjon, token: Token) {
        oppgaveGateway.endre(
            token = token,
            oppgaveId = id,
            request = PatchOppgaveRequest(
                versjon = versjon.asLong(),
                tilordnetRessurs = null)
        )
    }

    suspend fun søk(token: Token): Result<SøkOppgaverResponse> {
        return oppgaveGateway.søk(
            token = token,
            params = SøkQueryParams(
                tema = listOf("AAP"),
                statuskategori = Statuskategori.AAPEN,
            )
        )
    }

    suspend fun hent(token: Token, oppgaveId: OppgaveId): Result<OpprettResponse> {
        return oppgaveGateway.hent(
            token = token,
            oppgaveId = oppgaveId)
    }
}