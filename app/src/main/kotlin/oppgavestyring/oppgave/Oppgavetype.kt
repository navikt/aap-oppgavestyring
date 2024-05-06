package oppgavestyring.oppgave

enum class Oppgavetype(private val kode: String) {

    BEHANDLE_SAK("STARTV"), //TODO: bruker startv grunnet mangel på bedre verdi, burde endres til noe mer passende
    AVKLARINGSBEHOV("TODO");

    fun kode() = kode
}