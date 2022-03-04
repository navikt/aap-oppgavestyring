package no.nav.aap.app

import no.nav.aap.avro.manuell.v1.*
import java.time.LocalDate

data class DtoManuell(
    val løsning_11_2_manuell: DtoLøsningParagraf_11_2? = null,
    val løsning_11_3_manuell: DtoLøsningParagraf_11_3? = null,
    val løsning_11_4_ledd2_ledd3_manuell: DtoLøsningParagraf_11_4_ledd2_ledd3? = null,
    val løsning_11_5_manuell: DtoLøsningParagraf_11_5? = null,
    val løsning_11_6_manuell: DtoLøsningParagraf_11_6? = null,
    val løsning_11_12_ledd1_manuell: DtoLøsningParagraf_11_12_ledd1? = null,
    val løsning_11_29_manuell: DtoLøsningParagraf_11_29? = null,
    val løsningVurderingAvBeregningsdato: DtoLøsningVurderingAvBeregningsdato? = null,
)

data class DtoLøsningParagraf_11_2(val erMedlem: String)
data class DtoLøsningParagraf_11_3(val erOppfylt: Boolean)
data class DtoLøsningParagraf_11_4_ledd2_ledd3(val erOppfylt: Boolean)
data class DtoLøsningParagraf_11_5(val grad: Int)
data class DtoLøsningParagraf_11_6(val erOppfylt: Boolean)
data class DtoLøsningParagraf_11_12_ledd1(val erOppfylt: Boolean)
data class DtoLøsningParagraf_11_29(val erOppfylt: Boolean)
data class DtoLøsningVurderingAvBeregningsdato(val beregningsdato: LocalDate)

fun DtoManuell.toAvro(): Manuell = Manuell(
    løsning_11_2_manuell?.let { Losning_11_2(it.erMedlem) },
    løsning_11_3_manuell?.let { Losning_11_3(it.erOppfylt) },
    løsning_11_4_ledd2_ledd3_manuell?.let { Losning_11_4_l2_l3(it.erOppfylt) },
    løsning_11_5_manuell?.let { Losning_11_5(it.grad) },
    løsning_11_6_manuell?.let { Losning_11_6(it.erOppfylt) },
    løsning_11_12_ledd1_manuell?.let { Losning_11_12_l1(it.erOppfylt) },
    løsning_11_29_manuell?.let { Losning_11_29(it.erOppfylt) },
    løsningVurderingAvBeregningsdato?.let { LosningVurderingAvBeregningsdato(it.beregningsdato) }
)
