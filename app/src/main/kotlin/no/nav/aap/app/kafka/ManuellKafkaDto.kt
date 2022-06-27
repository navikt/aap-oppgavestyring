package no.nav.aap.app.kafka

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year


data class Løsning_11_2_yrkesskade_manuell(
    val vurdertAv: String,
    val tidspunktForVurdering: LocalDateTime,
    val erMedlem: String
)

data class Løsning_11_2_manuell(
    val vurdertAv: String,
    val tidspunktForVurdering: LocalDateTime,
    val erMedlem: String
)

data class Løsning_11_3_manuell(
    val vurdertAv: String,
    val tidspunktForVurdering: LocalDateTime,
    val erOppfylt: Boolean
)

data class Løsning_11_4_ledd2_ledd3_manuell(
    val vurdertAv: String,
    val tidspunktForVurdering: LocalDateTime,
    val erOppfylt: Boolean
)

data class Løsning_11_5_manuell(
    val vurdertAv: String,
    val tidspunktForVurdering: LocalDateTime,
    val kravOmNedsattArbeidsevneErOppfylt: Boolean,
    val nedsettelseSkyldesSykdomEllerSkade: Boolean
)

data class Løsning_11_5_yrkesskade_manuell(
    val vurdertAv: String,
    val tidspunktForVurdering: LocalDateTime,
    val arbeidsevneErNedsattMedMinst50Prosent: Boolean,
    val arbeidsevneErNedsattMedMinst30Prosent: Boolean
)

data class Løsning_11_6_manuell(
    val vurdertAv: String,
    val tidspunktForVurdering: LocalDateTime,
    val harBehovForBehandling: Boolean,
    val harBehovForTiltak: Boolean,
    val harMulighetForÅKommeIArbeid: Boolean
)

data class Løsning_11_12_ledd1_manuell(
    val vurdertAv: String,
    val tidspunktForVurdering: LocalDateTime,
    val bestemmesAv: String,
    val unntak: String,
    val unntaksbegrunnelse: String,
    val manueltSattVirkningsdato: LocalDate
)

data class Løsning_11_22_manuell(
    val vurdertAv: String,
    val tidspunktForVurdering: LocalDateTime,
    val erOppfylt: Boolean,
    val andelNedsattArbeidsevne: Int,
    val år: Year,
    val antattÅrligArbeidsinntekt: Double
)

data class Løsning_11_29_manuell(
    val vurdertAv: String,
    val tidspunktForVurdering: LocalDateTime,
    val erOppfylt: Boolean
)

data class Løsning_beregningsdato_manuell(
    val vurdertAv: String,
    val tidspunktForVurdering: LocalDateTime,
    val beregningsdato: LocalDate
)
