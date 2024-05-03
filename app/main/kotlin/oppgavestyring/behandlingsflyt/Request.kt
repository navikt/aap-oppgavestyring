package oppgavestyring.behandlingsflyt

import java.util.UUID

data class Request(
    val saksnummer: String,
    val referanse: UUID,
    val personident: String,
    val avklaringsbehov: String,
    val status: Status
)

enum class Status {
    OPPRETTET,
    UTREDES,
    AVSLUTTET,
    PÅ_VENT
}
