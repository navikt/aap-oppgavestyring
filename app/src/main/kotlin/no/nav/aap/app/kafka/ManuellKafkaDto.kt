package no.nav.aap.app.kafka

import java.time.LocalDate

data class ManuellKafkaDto(
    val vurdertAv: String,
    val løsning_11_2_manuell: Løsning_11_2_manuell? = null,
    val løsning_11_3_manuell: Løsning_11_3_manuell? = null,
    val løsning_11_4_ledd2_ledd3_manuell: Løsning_11_4_ledd2_ledd3_manuell? = null,
    val løsning_11_5_manuell: Løsning_11_5_manuell? = null,
    val løsning_11_6_manuell: Løsning_11_6_manuell? = null,
    val løsning_11_12_ledd1_manuell: Løsning_11_12_ledd1_manuell? = null,
    val løsning_11_29_manuell: Løsning_11_29_manuell? = null,
    val løsningVurderingAvBeregningsdato: LøsningVurderingAvBeregningsdato? = null,
)

data class Løsning_11_2_manuell(val erMedlem: String)
data class Løsning_11_3_manuell(val erOppfylt: Boolean)
data class Løsning_11_4_ledd2_ledd3_manuell(val erOppfylt: Boolean)
data class Løsning_11_5_manuell(
    val kravOmNedsattArbeidsevneErOppfylt: Boolean,
    val nedsettelseSkyldesSykdomEllerSkade: Boolean
)

data class Løsning_11_6_manuell(
    val harBehovForBehandling: Boolean,
    val harBehovForTiltak: Boolean,
    val harMulighetForÅKommeIArbeid: Boolean
)

data class Løsning_11_12_ledd1_manuell(
    val bestemmesAv: String,
    val unntak: String,
    val unntaksbegrunnelse: String,
    val manueltSattVirkningsdato: LocalDate
)

data class Løsning_11_29_manuell(val erOppfylt: Boolean)
data class LøsningVurderingAvBeregningsdato(val beregningsdato: LocalDate)
