package no.nav.aap.app

import no.nav.aap.app.kafka.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth
import java.util.*

data class DtoLøsningParagraf_11_2(val erMedlem: String) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_2_manuell(vurdertAv, LocalDateTime.now(), erMedlem)
}

data class DtoLøsningParagraf_11_3(val erOppfylt: Boolean) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_3_manuell(vurdertAv, LocalDateTime.now(), erOppfylt)
}

data class DtoLøsningParagraf_11_4_ledd2_ledd3(val erOppfylt: Boolean) {
    internal fun toKafkaDto(vurdertAv: String) =
        Løsning_11_4_ledd2_ledd3_manuell(vurdertAv, LocalDateTime.now(), erOppfylt)
}

data class DtoLøsningParagraf_11_5(
    val kravOmNedsattArbeidsevneErOppfylt: Boolean,
    val nedsettelseSkyldesSykdomEllerSkade: Boolean
) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_5_manuell(
        vurdertAv = vurdertAv,
        tidspunktForVurdering = LocalDateTime.now(),
        kravOmNedsattArbeidsevneErOppfylt = kravOmNedsattArbeidsevneErOppfylt,
        nedsettelseSkyldesSykdomEllerSkade = nedsettelseSkyldesSykdomEllerSkade
    )
}

data class DtoLøsningParagraf_11_6(
    val harBehovForBehandling: Boolean,
    val harBehovForTiltak: Boolean,
    val harMulighetForÅKommeIArbeid: Boolean
) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_6_manuell(
        vurdertAv = vurdertAv,
        tidspunktForVurdering = LocalDateTime.now(),
        harBehovForBehandling = harBehovForBehandling,
        harBehovForTiltak = harBehovForTiltak,
        harMulighetForÅKommeIArbeid = harMulighetForÅKommeIArbeid
    )
}

data class DtoLøsningParagraf_11_12_ledd1(
    val bestemmesAv: String,
    val unntak: String,
    val unntaksbegrunnelse: String,
    val manueltSattVirkningsdato: LocalDate
) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_12_ledd1_manuell(
        vurdertAv = vurdertAv,
        tidspunktForVurdering = LocalDateTime.now(),
        bestemmesAv = bestemmesAv,
        unntak = unntak,
        unntaksbegrunnelse = unntaksbegrunnelse,
        manueltSattVirkningsdato = manueltSattVirkningsdato
    )
}

data class DtoLøsningParagraf_11_19(
    val beregningsdato: LocalDate
) {
    internal fun toKafkaDto(vurdertAv: String) =
        Løsning_11_19_manuell(vurdertAv, LocalDateTime.now(), beregningsdato)
}

data class DtoLøsningParagraf_11_29(val erOppfylt: Boolean) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_29_manuell(vurdertAv, LocalDateTime.now(), erOppfylt)
}

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
    val løsningVurderingAvBeregningsdato: DtoLøsningParagraf_11_19?
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
