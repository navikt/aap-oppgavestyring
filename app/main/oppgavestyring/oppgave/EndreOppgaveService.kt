package oppgavestyring.oppgave

import oppgavestyring.oppgave.adapter.Oppgave
import oppgavestyring.oppgave.adapter.PatchOppgaveRequest
import oppgavestyring.oppgave.adapter.Token

class EndreOppgaveService(private val oppgaveClient: Oppgave) {

    suspend fun tildelRessursTilOppgave(id: Long, navIdent: NavIdent, token: Token) {
        oppgaveClient.endre(
            token,
            PatchOppgaveRequest(
                id = id,
                versjon = 1,
                tilordnetRessurs = navIdent.asString()))
    }
}