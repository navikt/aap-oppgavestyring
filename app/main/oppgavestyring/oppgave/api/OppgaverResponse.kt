package oppgavestyring.oppgave.api

data class OppgaverResponse(
    val oppgaver: List<Oppgave>,
)

data class Oppgave(
    val oppgaveId: Long,
    val versjon: Int,
    val oppgavetype: Oppgavetype,
    val foedselsnummer: String, //innbygger
    val opprettet: String,
    val tilordnetRessurs: String? = null,
    val reservertTil: String? = null
)

enum class Oppgavetype {
    AVKLARINGSBEHOV
}