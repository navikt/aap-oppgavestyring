package no.nav.aap.app.axsys

import no.nav.aap.app.RoleName
import no.nav.aap.app.frontendView.Autorisasjon

data class InnloggetBruker(
    val ident: String,
    val roller: List<RoleName>,
    val tilknyttedeEnheter: List<String>,
    //FIXME
    val harSkjermingsrolle: Boolean = false,
) {
    fun adressebeskyttelseRoller() =
        roller.filter { it in listOf(RoleName.FORTROLIG_ADRESSE, RoleName.STRENGT_FORTROLIG_ADRESSE) } + RoleName.UGRADERT

    fun harSkjermingsrolle() = harSkjermingsrolle
    fun erTilknyttetNAY() = roller.any { it in listOf(RoleName.SAKSBEHANDLER, RoleName.BESLUTTER) }
    fun erTilknyttetLokalkontor() = roller.any { it in listOf(RoleName.VEILEDER, RoleName.FATTER) }
    fun tilknyttedeEnheter() = tilknyttedeEnheter

    internal fun hentAutorisasjonForNAY(): Autorisasjon {
        //FIXME: Mangler skille mellom ENDRE og GODKJENNE
        return if (erTilknyttetNAY()) Autorisasjon.ENDRE else Autorisasjon.LESE
    }

    internal fun hentAutorisasjonForLokalkontor(): Autorisasjon {
        //FIXME: Mangler skille mellom ENDRE og GODKJENNE
        return if (erTilknyttetLokalkontor()) Autorisasjon.ENDRE else Autorisasjon.LESE
    }
}
