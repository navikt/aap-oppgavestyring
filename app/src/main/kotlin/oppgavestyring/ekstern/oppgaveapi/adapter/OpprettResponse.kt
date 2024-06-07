package oppgavestyring.ekstern.oppgaveapi.adapter

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