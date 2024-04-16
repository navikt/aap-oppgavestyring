package oppgavestyring.oppgave.db

import oppgavestyring.oppgave.OppgaveRepository
import oppgavestyring.oppgave.adapter.OpprettResponse

object FakeOppgaveRepository : OppgaveRepository {

    private val map : MutableMap<Long, OpprettResponse> = HashMap()

    override fun lagre(nyOppgave: OpprettResponse) {
        map.put(nyOppgave.id, nyOppgave)
    }

}
