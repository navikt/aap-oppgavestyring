package no.nav.aap.app.axsys

import no.nav.aap.app.RoleName
import no.nav.aap.app.frontendView.Autorisasjon
import no.nav.aap.app.kafka.SøkereKafkaDto

data class InnloggetBruker(
    val brukernavn: String,
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

    internal fun hentAutorisasjonForNAY(beregningsdato: SøkereKafkaDto.VurderingAvBeregningsdato): Autorisasjon {
        if (!erTilknyttetNAY()) return Autorisasjon.LESE
        if (roller.none { it == RoleName.BESLUTTER }) return Autorisasjon.ENDRE

        return beregningsdato.løsningVurderingAvBeregningsdato?.hentAutorisasjon(brukernavn) ?: Autorisasjon.LESE
    }

    internal fun hentAutorisasjonForNAY(vilkårsvurdering: SøkereKafkaDto.Vilkårsvurdering): Autorisasjon {
        if (!erTilknyttetNAY()) return Autorisasjon.LESE
        if (roller.none { it == RoleName.BESLUTTER }) return Autorisasjon.ENDRE

        return vilkårsvurdering.hentAutorisasjon(brukernavn)
    }

    internal fun hentAutorisasjonForLokalkontor(vilkårsvurdering: SøkereKafkaDto.Vilkårsvurdering): Autorisasjon {
        if (!erTilknyttetLokalkontor()) return Autorisasjon.LESE
        if (roller.none { it == RoleName.FATTER }) return Autorisasjon.ENDRE

        return vilkårsvurdering.hentAutorisasjon(brukernavn)
    }
}
