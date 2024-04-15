package oppgavestyring.oppgave

data class OppgaveId(private val id: Long) {
    fun asLong() : Long = id
}
