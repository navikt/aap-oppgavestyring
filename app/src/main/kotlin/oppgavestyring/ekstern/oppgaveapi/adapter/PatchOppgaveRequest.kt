package oppgavestyring.ekstern.oppgaveapi.adapter

data class PatchOppgaveRequest(
    val versjon: Long,
    //val status : Status? = null,
    val tilordnetRessurs: String? = null // nav-ident
)