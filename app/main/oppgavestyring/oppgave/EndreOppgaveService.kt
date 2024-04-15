package oppgavestyring.oppgave

import oppgavestyring.oppgave.adapter.Oppgave
import oppgavestyring.oppgave.adapter.PatchOppgaveRequest
import oppgavestyring.oppgave.adapter.Token

class EndreOppgaveService(private val oppgaveClient: Oppgave) {

    suspend fun tildelRessursTilOppgave(id: OppgaveId, versjon: Versjon, navIdent: NavIdent, token: Token) {
        oppgaveClient.endre(
            token,
            PatchOppgaveRequest(
                id = id.asLong(),
                versjon = versjon.asLong(),
                tilordnetRessurs = navIdent.asString()))
    }
}