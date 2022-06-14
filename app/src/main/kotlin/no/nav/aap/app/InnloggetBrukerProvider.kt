package no.nav.aap.app

import io.ktor.server.auth.jwt.*
import no.nav.aap.app.axsys.AxsysClient
import no.nav.aap.app.axsys.InnloggetBruker
import java.util.*

class InnloggetBrukerProvider(private val axsysClient: AxsysClient, private val configRoles: List<Role>) {

    suspend fun hentInnloggetBruker(principal: JWTPrincipal): InnloggetBruker {
        val ident = requireNotNull(principal.getClaim("NAVident", String::class)) {"NAVident er null i token"}
        val brukernavn = requireNotNull(principal.getClaim("preferred_username", String::class)) {"preferred_username er null i token"}

        val roller = principal.getListClaim("groups", UUID::class)
            .map { claimObjectId -> configRoles.single { configRole -> configRole.objectId == claimObjectId }.name }
        return InnloggetBruker(
            brukernavn = brukernavn,
            roller = roller,
            tilknyttedeEnheter = axsysClient.hentEnheter(ident)
        )
    }

}