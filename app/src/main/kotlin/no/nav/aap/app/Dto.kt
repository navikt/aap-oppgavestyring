package no.nav.aap.app

import no.nav.aap.app.kafka.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth
import java.util.*

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
data class DtoLøsningParagraf_11_5(
    val kravOmNedsattArbeidsevneErOppfylt: Boolean,
    val nedsettelseSkyldesSykdomEllerSkade: Boolean
)

data class DtoLøsningParagraf_11_6(
    val harBehovForBehandling: Boolean,
    val harBehovForTiltak: Boolean,
    val harMulighetForÅKommeIArbeid: Boolean
)

data class DtoLøsningParagraf_11_12_ledd1(
    val bestemmesAv: String,
    val unntak: String,
    val unntaksbegrunnelse: String,
    val manueltSattVirkningsdato: LocalDate
)

data class DtoLøsningParagraf_11_29(val erOppfylt: Boolean)
data class DtoLøsningVurderingAvBeregningsdato(val beregningsdato: LocalDate)

fun DtoManuell.toKafkaDto(vurdertAv: String): ManuellKafkaDto = ManuellKafkaDto(
    vurdertAv = vurdertAv,
    løsning_11_2_manuell = løsning_11_2_manuell?.let { Løsning_11_2_manuell(it.erMedlem) },
    løsning_11_3_manuell = løsning_11_3_manuell?.let { Løsning_11_3_manuell(it.erOppfylt) },
    løsning_11_4_ledd2_ledd3_manuell = løsning_11_4_ledd2_ledd3_manuell?.let { Løsning_11_4_ledd2_ledd3_manuell(it.erOppfylt) },
    løsning_11_5_manuell = løsning_11_5_manuell?.let {
        Løsning_11_5_manuell(
            it.kravOmNedsattArbeidsevneErOppfylt,
            it.nedsettelseSkyldesSykdomEllerSkade
        )
    },
    løsning_11_6_manuell = løsning_11_6_manuell?.let {
        Løsning_11_6_manuell(
            harBehovForBehandling = it.harBehovForBehandling,
            harBehovForTiltak = it.harBehovForTiltak,
            harMulighetForÅKommeIArbeid = it.harMulighetForÅKommeIArbeid
        )
    },
    løsning_11_12_ledd1_manuell = løsning_11_12_ledd1_manuell?.let {
        Løsning_11_12_ledd1_manuell(
            bestemmesAv = it.bestemmesAv,
            unntak = it.unntak,
            unntaksbegrunnelse = it.unntaksbegrunnelse,
            manueltSattVirkningsdato = it.manueltSattVirkningsdato
        )
    },
    løsning_11_29_manuell = løsning_11_29_manuell?.let { Løsning_11_29_manuell(it.erOppfylt) },
    løsningVurderingAvBeregningsdato = løsningVurderingAvBeregningsdato?.let { LøsningVurderingAvBeregningsdato(it.beregningsdato) }
)

data class DtoSøker(
    val personident: String,
    val geografiskTilknytning: String,
    val diskresjonskode: String,
    val skjermet: Boolean,
    val lokalkontorEnhetsnummer: String,
    val saker: List<DtoSak>
)

data class DtoSak(
    val saksid: UUID,
    val tilstand: String,
    val sakstyper: List<DtoSakstype>,
    val vurderingsdato: LocalDate,
    val vurderingAvBeregningsdato: DtoVurderingAvBeregningsdato,
    val vedtak: DtoVedtak?
)

data class DtoSakstype(
    val type: String,
    val vilkårsvurderinger: List<DtoVilkårsvurdering>
)

data class DtoVilkårsvurdering(
    val vilkårsvurderingid: UUID,
    val paragraf: String,
    val ledd: List<String>,
    val tilstand: String,
    val måVurderesManuelt: Boolean,
    val løsning_11_2_maskinell: DtoLøsningParagraf_11_2? = null,
    val løsning_11_2_manuell: DtoLøsningParagraf_11_2? = null,
    val løsning_11_3_manuell: DtoLøsningParagraf_11_3? = null,
    val løsning_11_4_ledd2_ledd3_manuell: DtoLøsningParagraf_11_4_ledd2_ledd3? = null,
    val løsning_11_5_manuell: DtoLøsningParagraf_11_5? = null,
    val løsning_11_6_manuell: DtoLøsningParagraf_11_6? = null,
    val løsning_11_12_ledd1_manuell: DtoLøsningParagraf_11_12_ledd1? = null,
    val løsning_11_29_manuell: DtoLøsningParagraf_11_29? = null,
)

data class DtoVurderingAvBeregningsdato(
    val tilstand: String,
    val løsningVurderingAvBeregningsdato: DtoLøsningVurderingAvBeregningsdato?
)

data class DtoVedtak(
    val innvilget: Boolean,
    val inntektsgrunnlag: DtoInntektsgrunnlag,
    val søknadstidspunkt: LocalDateTime,
    val vedtaksdato: LocalDate,
    val virkningsdato: LocalDate
)

data class DtoInntektsgrunnlag(
    val beregningsdato: LocalDate,
    val inntekterSiste3Kalenderår: List<DtoInntektsgrunnlagForÅr>,
    val fødselsdato: LocalDate,
    val sisteKalenderår: Year,
    val grunnlagsfaktor: Double
)

data class DtoInntektsgrunnlagForÅr(
    val år: Year,
    val inntekter: List<DtoInntekt>,
    val beløpFørJustering: Double,
    val beløpJustertFor6G: Double,
    val erBeløpJustertFor6G: Boolean,
    val grunnlagsfaktor: Double
)

data class DtoInntekt(
    val arbeidsgiver: String,
    val inntektsmåned: YearMonth,
    val beløp: Double
)

data class DtoInntekter(
    val inntekter: List<DtoInntekt>
)
