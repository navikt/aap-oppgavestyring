package oppgavestyring.oppgave

import oppgavestyring.oppgave.adapter.PatchOppgaveRequest
import oppgavestyring.oppgave.adapter.Token

class EndreOppgaveService(private val oppgaveGateway: OppgaveGateway) {

    suspend fun tildelRessursTilOppgave(id: OppgaveId, versjon: Versjon, navIdent: NavIdent, token: Token) {
        oppgaveGateway.endre(
            token,
            PatchOppgaveRequest(
                id = id.asLong(),
                versjon = versjon.asLong(),
                tilordnetRessurs = navIdent.asString()))
    }
}