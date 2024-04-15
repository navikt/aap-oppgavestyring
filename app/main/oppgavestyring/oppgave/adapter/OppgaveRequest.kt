package oppgavestyring.oppgave.adapter

data class OpprettRequest(
    val oppgavetype: String, // se kodeverk
    val tema: String, // se kodeverk
    val prioritet: Prioritet,
    val aktivDato: String, // dato
    val personident: String? = null, // 11 - 13 tegn
    val orgnr: String? = null,
    val tildeltEnhetsnr: NavEnhet? = null, // 4 tegn
    val opprettetAvEnhetsnr: NavEnhet? = null, // 4 tegn
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val tilordnetRessurs: String? = null, // navident
    val beskrivelse: String? = null,
    val behandlingstema: String? = null, // se kodeverk
    val behandlingstype: String? = null, // se kodeverk
    val fristFerdigstillelse: String? = null, // dato
)

typealias NavEnhet = String

enum class Statuskategori {
    AAPEN, AVSLUTTET
}