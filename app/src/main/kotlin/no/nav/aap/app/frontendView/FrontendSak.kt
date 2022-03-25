package no.nav.aap.app.frontendView

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth
import java.util.*

data class FrontendSak(
    val saksid: UUID,
    val tilstand: String,
    val sakstype: FrontendSakstype?,
    val søknadstidspunkt: LocalDateTime,
    val vedtak: FrontendVedtak?
)

data class FrontendSakstype(
    val type: String,
    val aktiv: Boolean,
    val vilkårsvurderinger: List<FrontendVilkårsvurdering>
)

data class FrontendVilkårsvurdering(
    val vilkårsvurderingsid: UUID,
    val paragraf: String,
    val ledd: List<String>,
    val tilstand: String,
    val måVurderesManuelt: Boolean
)

data class FrontendVedtak(
    val vedtaksid: UUID,
    val innvilget: Boolean,
    val inntektsgrunnlag: FrontendInntektsgrunnlag,
    val vedtaksdato: LocalDate,
    val virkningsdato: LocalDate
)

data class FrontendInntektsgrunnlag(
    val beregningsdato: LocalDate,
    val inntekterSiste3Kalenderår: List<FrontendInntektsgrunnlagForÅr>,
    val fødselsdato: LocalDate,
    val sisteKalenderår: Year,
    val grunnlagsfaktor: Double
)

data class FrontendInntektsgrunnlagForÅr(
    val år: Year,
    val inntekter: List<FrontendInntekt>,
    val beløpFørJustering: Double,
    val beløpJustertFor6G: Double,
    val erBeløpJustertFor6G: Boolean,
    val grunnlagsfaktor: Double
)

data class FrontendInntekt(
    val arbeidsgiver: String,
    val inntekstmåned: YearMonth,
    val beløp: Double
)
