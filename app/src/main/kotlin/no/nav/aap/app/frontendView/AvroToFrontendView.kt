package no.nav.aap.app.frontendView

import no.nav.aap.avro.sokere.v1.*
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth
import java.util.*

internal fun Soker.toFrontendView(): FrontendSøker =
    FrontendSøker(
        personident = personident,
        fødselsdato = fodselsdato,
        sak = saker.first().toFrontendView()
    )

private fun Sak.toFrontendView(): FrontendSak = FrontendSak(
    saksid = saksid?.let(UUID::fromString) ?: UUID.randomUUID(),
    tilstand = tilstand,
    sakstype = sakstyper.map(Sakstype::toFrontendView).first { it.aktiv },
    søknadstidspunkt = soknadstidspunkt ?: LocalDateTime.now(),
    vedtak = vedtak?.toFrontendView()
)

private fun Sakstype.toFrontendView() = FrontendSakstype(
    type = type,
    aktiv = aktiv ?: true,
    vilkårsvurderinger = vilkarsvurderinger.map(Vilkarsvurdering::toFrontendView)
)

private fun Vilkarsvurdering.toFrontendView() = FrontendVilkårsvurdering(
    vilkårsvurderingsid = vilkarsvurderingsid?.let(UUID::fromString) ?: UUID.randomUUID(),
    paragraf = paragraf,
    ledd = ledd,
    tilstand = tilstand,
    måVurderesManuelt = maVurderesManuelt
)

private fun Vedtak.toFrontendView() = FrontendVedtak(
    vedtaksid = vedtaksid?.let(UUID::fromString) ?: UUID.randomUUID(),
    innvilget = innvilget,
    inntektsgrunnlag = inntektsgrunnlag.toFrontendView(),
    vedtaksdato = vedtaksdato,
    virkningsdato = virkningsdato
)

private fun Inntektsgrunnlag.toFrontendView() = FrontendInntektsgrunnlag(
    beregningsdato = beregningsdato,
    inntekterSiste3Kalenderår = inntekterSiste3Kalenderar.map { it.toFrontendView() },
    fødselsdato = fodselsdato,
    sisteKalenderår = Year.from(sisteKalenderar),
    grunnlagsfaktor = grunnlagsfaktor
)

private fun InntektsgrunnlagForAr.toFrontendView() = FrontendInntektsgrunnlagForÅr(
    år = Year.from(ar),
    inntekter = inntekter.map { it.toFrontendView() },
    beløpFørJustering = belopForJustering,
    beløpJustertFor6G = belopJustertFor6G,
    erBeløpJustertFor6G = erBelopJustertFor6G,
    grunnlagsfaktor = grunnlagsfaktor
)

private fun Inntekt.toFrontendView() = FrontendInntekt(
    arbeidsgiver = arbeidsgiver,
    inntekstmåned = YearMonth.from(inntektsmaned),
    beløp = belop
)
