package no.nav.aap.app.frontendView

import no.nav.aap.avro.sokere.v1.*
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth

internal fun Soker.toFrontendView(): List<FrontendSak> = saker.map { it.toFrontendView(personident, fodselsdato) }

private fun Sak.toFrontendView(personident: String, fodselsdato: LocalDate): FrontendSak = FrontendSak(
    personident = personident,
    fødselsdato = fodselsdato,
    tilstand = tilstand,
    sakstype = FrontendSakstype( //FIXME
        type = "STANDARD",
        vilkårsvurderinger = vilkarsvurderinger.map { it.toFrontendView() }),
    vedtak = vedtak?.toFrontendView()
)

private fun Vilkarsvurdering.toFrontendView() = FrontendVilkårsvurdering(
    paragraf = paragraf,
    ledd = ledd,
    tilstand = tilstand,
    harÅpenOppgave = false
)

private fun Vedtak.toFrontendView() = FrontendVedtak(
    innvilget = innvilget,
    inntektsgrunnlag = inntektsgrunnlag.toFrontendView(),
    søknadstidspunkt = soknadstidspunkt,
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