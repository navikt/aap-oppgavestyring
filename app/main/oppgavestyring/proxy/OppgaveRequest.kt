package oppgavestyring.proxy

import io.ktor.util.*

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
    val endretAvEnhetsnr: NavEnhet? = null,
    val opprettetAvEnhetsnr: NavEnhet? = null,
    val journalpostId: String? = null,
    val behandlesAvApplikasjon: String? = null,
    val aktoerId: String? = null,
    val orgnr: String? = null,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val tema: String,
    val behandlingstema: String? = null,
    val oppgavetype: String,
    val behandlingstype: String? = null,
    val versjon: Int,
    val mappeId: Long? = null,
    val opprettetAv: String? = null,
    val endretAv: String? = null,
    val prioritet: Prioritet,
    val status: Status,
    val fristFerdigstillelse: String? = null, // date (ISO-8601)
    val aktivDato: String, // date
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

data class SøkQueryParams(
    val statuskategori: Statuskategori? = null,
    val tema: List<String>? = null, // se kodeverk
    val oppgavetype: List<String>? = null, // se kodeverk
    val tildeltEnhetsnr: NavEnhet? = null,
    val tilordnetRessurs: String? = null,
    val behandlingstema: String? = null,
    val behandlingstype: String? = null,
    val aktoerId: List<String>? = null,
    val journalpostId: List<String>? = null,
    val saksreferanse: List<String>? = null,
    val orgnr: List<String>? = null,
    val limit: Long = 10,
    val offset: Long = 0,
) {
    fun stringValues(): StringValues {
        return StringValues.build {
            statuskategori?.let { append("statuskategori", statuskategori.name) }
            tema?.let { appendAll("tema", tema) }
            oppgavetype?.let { appendAll("oppgavetype", oppgavetype) }
            tildeltEnhetsnr?.let { append("tildeltEnhetsnr", tildeltEnhetsnr) }
            tilordnetRessurs?.let { append("tilordnetRessurs", tilordnetRessurs) }
            behandlingstema?.let { append("behandlingstema", behandlingstema) }
            behandlingstype?.let { append("behandlingstype", behandlingstype) }
            aktoerId?.let { appendAll("aktoerId", aktoerId) }
            journalpostId?.let { appendAll("journalpostId", journalpostId) }
            saksreferanse?.let { appendAll("saksreferanse", saksreferanse) }
            orgnr?.let { appendAll("orgnr", orgnr) }
        }
    }
}

enum class Statuskategori {
    AAPEN, AVSLUTTET
}