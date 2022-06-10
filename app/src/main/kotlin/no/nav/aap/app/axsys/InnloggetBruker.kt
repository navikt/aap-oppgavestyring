package no.nav.aap.app.axsys

data class InnloggetBruker(
    val ident: String,
    val roller: List<String>,
    val tilknyttedeEnheter: List<String>,
    //FIXME
    val harSkjermingsrolle: Boolean = false,
    //FIXME
    val erTilknyttetNAY: Boolean = true,
    //FIXME
    val erTilknyttetLokalkontor: Boolean = false
) {
    fun adressebeskyttelseRoller() = listOf("UGRADERT")
    fun harSkjermingsrolle() = harSkjermingsrolle
    fun erTilknyttetNAY() = erTilknyttetNAY
    fun erTilknyttetLokalkontor() = erTilknyttetLokalkontor
    fun tilknyttedeEnheter() = tilknyttedeEnheter
}
