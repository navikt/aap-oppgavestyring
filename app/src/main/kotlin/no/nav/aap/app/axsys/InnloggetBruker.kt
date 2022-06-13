package no.nav.aap.app.axsys

import no.nav.aap.app.RoleName

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
}
