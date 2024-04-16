package oppgavestyring.oppgave.db

import oppgavestyring.oppgave.OppgaveRepository
import oppgavestyring.oppgave.adapter.OpprettResponse

object FakeOppgaveRepository : OppgaveRepository {
    override fun lagre(nyOppgave: Result<OpprettResponse>) {
        TODO("Not yet implemented")
    }

}
