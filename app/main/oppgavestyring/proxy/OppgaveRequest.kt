package oppgavestyring.proxy

data class OpprettRequest(
    val fnr: Personident,
    val enhet: String,
    val tittel: String,
    val titler: List<String> = emptyList(),
)

data class Personident(
    val fnr: String
)
