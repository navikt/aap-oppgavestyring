package oppgavestyring.oppgave

data class NavIdent(private val navIdent: String) {

    init {
        if (navIdent.length == 0) throw IllegalArgumentException("navIdent kan ikke være blank")
        if (navIdent.length > 7) throw IllegalArgumentException("navIdent må være under 8 tegn")
    }
    fun asString(): String = navIdent
}
