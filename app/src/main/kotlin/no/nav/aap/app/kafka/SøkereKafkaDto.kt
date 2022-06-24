package no.nav.aap.app.kafka

import no.nav.aap.app.frontendView.Autorisasjon
import no.nav.aap.app.frontendView.Utfall
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth
import java.util.*

data class SøkereKafkaDto(
    val personident: String,
    val fødselsdato: LocalDate,
    val saker: List<Sak>,
    val version: Int,
) {
    internal companion object{
        internal const val VERSION = 2
    }

    data class Sak(
        val saksid: UUID,
        val tilstand: String,
        val sakstyper: List<Sakstype>,
        val vurderingsdato: LocalDate,
        val vurderingAvBeregningsdato: VurderingAvBeregningsdato,
        val søknadstidspunkt: LocalDateTime,
        val vedtak: Vedtak?
    )

    data class Sakstype(
        val type: String,
        val aktiv: Boolean,
        val vilkårsvurderinger: List<Vilkårsvurdering>,
    )

    // TODO: Vurdere hvordan løsning skal ligge i vilkårsvurdering
    data class Vilkårsvurdering(
        val vilkårsvurderingsid: UUID,
        val vurdertAv: String?,
        val godkjentAv: String?,
        val paragraf: String,
        val ledd: List<String>,
        val tilstand: String,
        val utfall: Utfall,
        val løsning_medlemskap_yrkesskade_maskinell: LøsningMaskinellMedlemskapYrkesskade? = null,
        val løsning_medlemskap_yrkesskade_manuell: LøsningManuellMedlemskapYrkesskade? = null,
        val løsning_11_2_maskinell: LøsningParagraf_11_2? = null,
        val løsning_11_2_manuell: LøsningParagraf_11_2? = null,
        val løsning_11_3_manuell: LøsningParagraf_11_3? = null,
        val løsning_11_4_ledd2_ledd3_manuell: LøsningParagraf_11_4_ledd2_ledd3? = null,
        val løsning_11_5_manuell: LøsningParagraf_11_5? = null,
        val løsning_11_5_yrkesskade_manuell: LøsningParagraf_11_5_yrkesskade? = null,
        val løsning_11_6_manuell: LøsningParagraf_11_6? = null,
        val løsning_11_12_ledd1_manuell: LøsningParagraf_11_12_ledd1? = null,
        val løsning_11_22_manuell: LøsningParagraf_11_22? = null,
        val løsning_11_29_manuell: LøsningParagraf_11_29? = null,
    ) {
        internal fun hentAutorisasjon(brukernavn: String): Autorisasjon {
            if (utfall == Utfall.IKKE_VURDERT) return Autorisasjon.ENDRE
            if (brukernavn == vurdertAv) return Autorisasjon.ENDRE
            return Autorisasjon.GODKJENNE
        }
    }

    data class LøsningMaskinellMedlemskapYrkesskade(val erMedlem: String)
    data class LøsningManuellMedlemskapYrkesskade(val vurdertAv: String, val erMedlem: String)
    data class LøsningParagraf_11_2(val vurdertAv: String, val erMedlem: String)
    data class LøsningParagraf_11_3(val vurdertAv: String, val erOppfylt: Boolean)
    data class LøsningParagraf_11_4_ledd2_ledd3(val vurdertAv: String, val erOppfylt: Boolean)

    data class LøsningParagraf_11_5(
        val vurdertAv: String,
        val kravOmNedsattArbeidsevneErOppfylt: Boolean,
        val nedsettelseSkyldesSykdomEllerSkade: Boolean
    )

    data class LøsningParagraf_11_5_yrkesskade(
        val vurdertAv: String,
        val arbeidsevneErNedsattMedMinst50Prosent: Boolean,
        val arbeidsevneErNedsattMedMinst30Prosent: Boolean
    )

    data class LøsningParagraf_11_6(
        val vurdertAv: String,
        val harBehovForBehandling: Boolean,
        val harBehovForTiltak: Boolean,
        val harMulighetForÅKommeIArbeid: Boolean
    )

    data class LøsningParagraf_11_12_ledd1(
        val vurdertAv: String,
        val bestemmesAv: String,
        val unntak: String,
        val unntaksbegrunnelse: String,
        val manueltSattVirkningsdato: LocalDate
    )

    data class LøsningParagraf_11_22(
        val vurdertAv: String,
        val erOppfylt: Boolean,
        val andelNedsattArbeidsevne: Int,
        val år: Year,
        val antattÅrligArbeidsinntekt: Double
    )

    data class LøsningParagraf_11_29(val vurdertAv: String, val erOppfylt: Boolean)

    data class Vedtak(
        val vedtaksid: UUID,
        val innvilget: Boolean,
        val inntektsgrunnlag: Inntektsgrunnlag,
        val vedtaksdato: LocalDate,
        val virkningsdato: LocalDate
    )

    data class Inntektsgrunnlag(
        val beregningsdato: LocalDate,
        val inntekterSiste3Kalenderår: List<InntekterForBeregning>,
        val yrkesskade: Yrkesskade?,
        val fødselsdato: LocalDate,
        val sisteKalenderår: Year,
        val grunnlagsfaktor: Double
    )

    data class InntekterForBeregning(
        val inntekter: List<Inntekt>,
        val inntektsgrunnlagForÅr: InntektsgrunnlagForÅr
    )

    data class VurderingAvBeregningsdato(
        val tilstand: String,
        val løsningVurderingAvBeregningsdato: LøsningVurderingAvBeregningsdato?
    )

    data class LøsningVurderingAvBeregningsdato(
        val vurdertAv: String,
        val beregningsdato: LocalDate
    ) {
        internal fun hentAutorisasjon(brukernavn: String): Autorisasjon {
            if (brukernavn == vurdertAv) return Autorisasjon.ENDRE
            return Autorisasjon.GODKJENNE
        }
    }

    data class Inntekt(
        val arbeidsgiver: String,
        val inntekstmåned: YearMonth,
        val beløp: Double
    )

    data class InntektsgrunnlagForÅr(
        val år: Year,
        val beløpFørJustering: Double,
        val beløpJustertFor6G: Double,
        val erBeløpJustertFor6G: Boolean,
        val grunnlagsfaktor: Double
    )

    data class Yrkesskade(
        val gradAvNedsattArbeidsevneKnyttetTilYrkesskade: Double,
        val inntektsgrunnlag: InntektsgrunnlagForÅr
    )
}
