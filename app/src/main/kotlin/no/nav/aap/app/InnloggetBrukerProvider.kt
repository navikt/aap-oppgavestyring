package no.nav.aap.app

import io.ktor.server.auth.jwt.*
import no.nav.aap.app.axsys.AxsysClient
import no.nav.aap.app.axsys.InnloggetBruker
import java.util.*

class InnloggetBrukerProvider(private val axsysClient: AxsysClient, private val configRoles: List<Role>) {

    suspend fun hentInnloggetBruker(principal: JWTPrincipal): InnloggetBruker {
        val ident = principal.getClaim("NAVident", String::class) ?: "ukjent"
        val roller = principal.getListClaim("groups", UUID::class)
            .map { claimObjectId -> configRoles.single { configRole -> configRole.objectId == claimObjectId }.name }
        return InnloggetBruker(
            ident = ident,
            roller = roller,
            tilknyttedeEnheter = axsysClient.hentEnheter(ident)
        )
    }

}