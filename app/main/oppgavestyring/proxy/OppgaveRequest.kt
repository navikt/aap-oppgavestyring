package oppgavestyring.proxy

data class OpprettRequest(
    val personident: String?, // 11 - 13 tegn
    val orgnr: String?,
    val tildeltEnhetsnr: NavEnhet?, // 4 tegn
    val opprettetAvEnhetsnr: NavEnhet?, // 4 tegn
    val journalpostId: String?,
    val behandlesAvApplikasjon: String?,
    val tilordnetRessurs: String?, // navident
    val beskrivelse: String?,
    val tema : String, // se kodeverk
    val behandlingstema: String?, // se kodeverk
    val oppgavetype: String, // se kodeverk
    val behandlingstype: String?, // se kodeverk
    val aktivDato : String, // dato
    val fristFerdigstillelse: String?, // dato
    val prioritet: Prioritet,
)

enum class Prioritet {
    HOY,
    NORM,
    LAV
}

typealias NavEnhet = String
