package oppgavestyring.oppgave.adapter

data class SøkOppgaverResponse(
    val antallTreffTotalt: Long,
    val oppgaver: List<OpprettResponse>
)