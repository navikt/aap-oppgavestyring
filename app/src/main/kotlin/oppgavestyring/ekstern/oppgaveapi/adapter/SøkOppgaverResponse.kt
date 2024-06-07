package oppgavestyring.ekstern.oppgaveapi.adapter

data class SøkOppgaverResponse(
    val antallTreffTotalt: Long,
    val oppgaver: List<OpprettResponse>
)