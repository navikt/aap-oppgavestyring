package no.nav.aap.app.axsys

data class InnloggetBruker(
    val ident: String,
    val roller: List<String>,
    val tilknyttetEnhet: List<String>,
    //FIXME
    val harSkjermingsrolle: Boolean = false
) {
    fun adressebeskyttelseRoller() = listOf("UGRADERT")
    fun harSkjermingsrolle() = harSkjermingsrolle
}
