package oppgavestyring.config.security

import java.util.*

enum class AdGruppe(private val gruppeId: UUID) {

    SAKSBEHANDLER(UUID.fromString("8bb0ee13-49cd-4e75-8c3d-a13420c8b376")),
    VEILEDER(UUID.fromString("12353679-aa80-4e59-bb47-95e727bfe85c")),
    AVDELINGSLEDER(UUID.fromString("f0f6cad5-e3c0-4308-99a2-3630ac60174a"));

    companion object {
        fun valueOf(gruppeId: UUID) = entries.find { gruppeId == it.gruppeId }
    }
}