package oppgavestyring.config.security

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import oppgavestyring.oppgave.NavIdent
import java.util.*

data class OppgavePrincipal(
    val ident: NavIdent,
    val gruppe: AdGruppe
): Principal {
    companion object {
        fun fromJwt(jwt: JWTCredential) = OppgavePrincipal(
            NavIdent(jwt.getClaim(NAV_IDENT_CLAIM_NAME, String::class) ?: throw IllegalArgumentException("JWT mangler Ident")),
            AdGruppe.KONTOR //AdGruppe.valueOf(jwt.getListClaim("groups", UUID::class).first())
        )
    }
}
