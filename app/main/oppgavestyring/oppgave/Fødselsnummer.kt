package oppgavestyring.oppgave

private const val REGEX = "^\\d{11}$"

data class Fødselsnummer(private val fødselsnummer: String) {

    init {
        if (!REGEX.toRegex().matches(fødselsnummer))
            throw IllegalArgumentException("Fødselsnummer må bestå av 11 tall")
    }

    fun asString() : String = fødselsnummer
}
