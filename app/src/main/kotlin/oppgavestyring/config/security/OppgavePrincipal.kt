package oppgavestyring.config.security

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import oppgavestyring.oppgave.NavIdent
import java.util.*

data class OppgavePrincipal(
    val ident: NavIdent,
    val grupper: List<AdGruppe>
): Principal {
    companion object {
        fun fromJwt(jwt: JWTCredential) = OppgavePrincipal(
            NavIdent(jwt.getClaim(NAV_IDENT_CLAIM_NAME, String::class) ?: throw IllegalArgumentException("JWT mangler Ident")),
            jwt.getListClaim("groups", UUID::class).map { AdGruppe.valueOf(it) }.filterNotNull()
        )
    }

    fun isVeileder() = AdGruppe.VEILEDER in grupper || AdGruppe.AVDELINGSLEDER in grupper
    fun isSaksbehandler() = AdGruppe.SAKSBEHANDLER in grupper || AdGruppe.AVDELINGSLEDER in grupper
    fun isAvdelingsleder() = AdGruppe.AVDELINGSLEDER in grupper
}
