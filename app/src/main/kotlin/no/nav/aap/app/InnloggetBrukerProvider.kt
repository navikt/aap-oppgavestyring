package no.nav.aap.app

import io.ktor.server.auth.jwt.*
import no.nav.aap.app.axsys.AxsysClient
import no.nav.aap.app.axsys.InnloggetBruker

class InnloggetBrukerProvider(private val axsysClient: AxsysClient) {

    suspend fun hentInnloggetBruker(principal: JWTPrincipal): InnloggetBruker {
        val ident = principal.getClaim("NAVident", String::class) ?: "ukjent"
        val roller = principal.getListClaim("groups", String::class)
        return InnloggetBruker(
            ident = ident,
            roller = roller,
            tilknyttedeEnheter = axsysClient.hentEnheter(ident)
        )
    }

}