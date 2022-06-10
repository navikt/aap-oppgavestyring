package no.nav.aap.app.axsys

data class InnloggetBruker(
    val ident: String,
    val roller: List<String>,
    val tilknyttedeEnheter: List<String>,
    //FIXME
    val harSkjermingsrolle: Boolean = false,
) {
    fun adressebeskyttelseRoller() =
        roller.filter { it in listOf("FORTROLIG_ADRESSE", "STRENGT_FORTROLIG_ADRESSE") } + "UGRADERT"

    fun harSkjermingsrolle() = harSkjermingsrolle
    fun erTilknyttetNAY() = roller.any { it in listOf("SAKSBEHANDLER", "BESLUTTER") }
    fun erTilknyttetLokalkontor() = roller.any { it in listOf("VEILEDER", "FATTER") }
    fun tilknyttedeEnheter() = tilknyttedeEnheter
}
