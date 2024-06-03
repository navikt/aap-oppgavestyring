package oppgavestyring.config.security

import java.util.UUID

enum class AdGruppe(private val gruppeIder: List<UUID>) {

    NAY(listOf(
        UUID.fromString("8bb0ee13-49cd-4e75-8c3d-a13420c8b376"),
        UUID.fromString("f0f6cad5-e3c0-4308-99a2-3630ac60174a")
    )),
    KONTOR(listOf(
        UUID.fromString("12353679-aa80-4e59-bb47-95e727bfe85c"),
        UUID.fromString("b60d74dd-fcf7-4c53-a50b-7b20f51804a1")
    ));

    companion object {
        fun valueOf(gruppeId: UUID) = entries.find { gruppeId in it.gruppeIder } ?: throw IllegalArgumentException("Gruppe $gruppeId not found")

    }
}