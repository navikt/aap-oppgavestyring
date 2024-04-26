package oppgavestyring.oppgave

enum class Oppgavetype(private val kode: String) {

    BEHANDLE_SAK("BEH_SAK"),
    AVKLARINGSBEHOV("TODO");

    fun kode() = kode
}