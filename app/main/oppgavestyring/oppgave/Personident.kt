package oppgavestyring.oppgave

private const val REGEX = "^\\d{11}$"

data class Personident(private val personident: String) {

    init {
        if (!REGEX.toRegex().matches(personident))
            throw IllegalArgumentException("Personident må bestå av 11 tall")
    }

    fun asString() : String = personident
}
