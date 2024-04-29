package oppgavestyring.oppgave

import oppgavestyring.oppgave.adapter.OpprettResponse

interface OppgaveRepository {
    fun lagre(nyOppgave: OpprettResponse)

}
