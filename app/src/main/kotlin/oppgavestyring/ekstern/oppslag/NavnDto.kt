package oppgavestyring.ekstern.oppslag

data class NavnDto(
    val fornavn: String,
    val etternavn: String,
) {
    override fun toString() = "$fornavn $etternavn"
}