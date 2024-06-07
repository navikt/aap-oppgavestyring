package oppgavestyring.intern.oppgave

private const val REGEX = "^[A-Å]\\d{6}$"

data class NavIdent(private val navIdent: String) {

    init {
        if (!REGEX.toRegex().matches(navIdent))
            throw IllegalArgumentException("Feil format på navIdent: $navIdent. Riktig format er stor bokstav og 6 tall.")
    }
    override fun toString(): String = navIdent
}
