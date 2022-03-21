package no.nav.aap.app.frontendView

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth
import java.util.UUID

data class FrontendSak(
    val saksid: UUID = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"), // TODO fiks i Avro
    val tilstand: String,
    val sakstype: FrontendSakstype?,
    val vedtak: FrontendVedtak?
)

data class FrontendSakstype(
    val type: String,
    val vilkårsvurderinger: List<FrontendVilkårsvurdering>
)

data class FrontendVilkårsvurdering(
    val paragraf: String,
    val ledd: List<String>,
    val tilstand: String,
    val måVurderesManuelt: Boolean
)

data class FrontendVedtak(
    val innvilget: Boolean,
    val inntektsgrunnlag: FrontendInntektsgrunnlag,
    val søknadstidspunkt: LocalDateTime,
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
