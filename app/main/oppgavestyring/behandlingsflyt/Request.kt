package oppgavestyring.behandlingsflyt

import java.util.UUID

data class Request(
    val saksnummer: String,
    val referanse: UUID,
    val personident: String,
    val beskrivelse: String
)