package no.nav.aap.app.kafka

import java.time.LocalDate

data class Løsning_11_2_manuell(val vurdertAv: String, val erMedlem: String)
data class Løsning_11_3_manuell(val vurdertAv: String, val erOppfylt: Boolean)
data class Løsning_11_4_ledd2_ledd3_manuell(val vurdertAv: String, val erOppfylt: Boolean)
data class Løsning_11_5_manuell(
    val vurdertAv: String,
    val kravOmNedsattArbeidsevneErOppfylt: Boolean,
    val nedsettelseSkyldesSykdomEllerSkade: Boolean
)

data class Løsning_11_6_manuell(
    val vurdertAv: String,
    val harBehovForBehandling: Boolean,
    val harBehovForTiltak: Boolean,
    val harMulighetForÅKommeIArbeid: Boolean
)

data class Løsning_11_12_ledd1_manuell(
    val vurdertAv: String,
    val bestemmesAv: String,
    val unntak: String,
    val unntaksbegrunnelse: String,
    val manueltSattVirkningsdato: LocalDate
)

data class Løsning_11_29_manuell(val vurdertAv: String, val erOppfylt: Boolean)
data class LøsningVurderingAvBeregningsdato(val vurdertAv: String, val beregningsdato: LocalDate)
