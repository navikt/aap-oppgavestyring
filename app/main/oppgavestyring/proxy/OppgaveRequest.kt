package oppgavestyring.proxy

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

enum class Prioritet {
    HOY,
    NORM,
    LAV
}

typealias NavEnhet = String

data class OpprettResponse(
    val id: Long,
    val tildeltEnhetsnr: NavEnhet,
    val tema: String,
    val oppgavetype: String,
    val versjon: Int,
    val prioritet: Prioritet,
    val status: Status,
    val aktivDato: String, // date
    val endretAvEnhetsnr: NavEnhet? = null,
    val opprettetAvEnhetsnr: NavEnhet? = null,
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val aktoerId: String? = null,
    val orgnr: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val behandlingstema: String? = null,
    val behandlingstype: String? = null,
    val mappeId: Long? = null,
    val opprettetAv: String? = null,
    val endretAv: String? = null,
    val fristFerdigstillelse: String? = null, // date (ISO-8601)
    val opprettetTidspunkt: String? = null, // date-time (ISO-8601)
    val ferdigstiltTidspunkt: String? = null, // date-time
    val endretTidspunkt: String? = null, // date-time
)

enum class Status {
    OPPRETTET,
    AAPNET,
    UNDER_BEHANDLING,
    FERDIGSTILT,
    FEILREGISTRERT
}