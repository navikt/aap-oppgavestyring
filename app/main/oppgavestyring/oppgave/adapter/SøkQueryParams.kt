package oppgavestyring.oppgave.adapter

import io.ktor.util.*

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