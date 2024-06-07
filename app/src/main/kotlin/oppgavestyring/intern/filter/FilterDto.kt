package oppgavestyring.intern.filter

class FilterDto(
    val id: Long,
    val tittel: String,
    val beskrivelse: String,
    val filter: String
) {
    companion object {
        fun fromBusinessObject(filter: OppgaveFilter) = FilterDto(
            id = filter.id.value,
            tittel = filter.tittel,
            beskrivelse = filter.beskrivelse,
            filter = filter.filter
        )
    }
}