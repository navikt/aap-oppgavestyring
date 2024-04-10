package oppgavestyring.oppgave.adapter

data class PatchOppgaveRequest(
    val versjon: Long, //påkrevd
    val status: Status,
    val tilordnetRessurs: String // nav-ident
)