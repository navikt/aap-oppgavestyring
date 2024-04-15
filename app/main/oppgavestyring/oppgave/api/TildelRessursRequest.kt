package oppgavestyring.oppgave.api

data class TildelRessursRequest(
    val id: Long,
    val versjon: Long,
    val navIdent: String
)
