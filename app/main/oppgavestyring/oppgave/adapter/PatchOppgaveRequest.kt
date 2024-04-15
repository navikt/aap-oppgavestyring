package oppgavestyring.oppgave.adapter

data class PatchOppgaveRequest(
    val id: Long,
    val versjon: Long,
    val status : Status? = null,
    val tilordnetRessurs: String? = null // nav-ident
)