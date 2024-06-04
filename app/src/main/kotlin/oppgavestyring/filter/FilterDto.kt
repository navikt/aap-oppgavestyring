package oppgavestyring.filter

class FilterDto(
    val tittel: String,
    val beskrivelse: String,
    val filter: String
) {
    companion object {
        fun fromBusinessObject(filter: OppgaveFilter) = FilterDto(
            tittel = filter.tittel,
            beskrivelse = filter.beskrivelse,
            filter = filter.filter
        )
    }
}