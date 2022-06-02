package no.nav.aap.app.modell

data class InnloggetBruker(
    val ident: String,
    val roller: List<String>,
    val tilknyttetEnhet: List<String>
) {
    fun adressebeskyttelseRoller() = listOf("UGRADERT")
}
