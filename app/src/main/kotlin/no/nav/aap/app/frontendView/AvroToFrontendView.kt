package no.nav.aap.app.frontendView

import no.nav.aap.app.modell.Paragraf
import no.nav.aap.avro.sokere.v1.*
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth
import java.util.*

internal fun Soker.toFrontendView(): FrontendSøker =
    FrontendSøker(
        personident = personident,
        fødselsdato = fodselsdato,
        sak = saker.first().toFrontendView(),
        skjermet = false // TODO: Hent fra avro Soker
    )

private fun Sak.toFrontendView(): FrontendSak = FrontendSak(
    saksid = saksid?.let(UUID::fromString) ?: UUID.randomUUID(),
    søknadstidspunkt = soknadstidspunkt ?: LocalDateTime.now(),
    type = sakstyper.first { it.aktiv }.type,
    vedtak = vedtak?.toFrontendView(),
    paragraf_11_2 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_2.name)?.toFrontendParagraf11_2(),
    paragraf_11_3 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_3.name)?.toFrontendParagraf11_3(),
    paragraf_11_4 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_4.name)?.toFrontendParagraf11_4(),
    paragraf_11_5 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_5.name)?.toFrontendParagraf11_5(),
    paragraf_11_6 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_6.name)?.toFrontendParagraf11_6(),
    paragraf_11_12 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_12.name)?.toFrontendParagraf11_12(),
    paragraf_11_29 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_29.name)?.toFrontendParagraf11_29()
)

private fun Iterable<Sakstype>.finnVilkårsvurdering(paragrafnavn: String) =
    this.first { it.aktiv }.vilkarsvurderinger.firstOrNull { it.paragraf == paragrafnavn }

private fun Vilkarsvurdering.toFrontendParagraf11_2() = FrontendParagraf_11_2(
    vilkårsvurderingsid = vilkarsvurderingsid?.let(UUID::fromString) ?: UUID.randomUUID(),
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = maVurderesManuelt
)

private fun Vilkarsvurdering.toFrontendParagraf11_3() = FrontendParagraf_11_3(
    vilkårsvurderingsid = vilkarsvurderingsid?.let(UUID::fromString) ?: UUID.randomUUID(),
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = maVurderesManuelt
)

private fun Vilkarsvurdering.toFrontendParagraf11_4() = FrontendParagraf_11_4(
    vilkårsvurderingsid = vilkarsvurderingsid?.let(UUID::fromString) ?: UUID.randomUUID(),
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = maVurderesManuelt
)

private fun Vilkarsvurdering.toFrontendParagraf11_5() = FrontendParagraf_11_5(
    vilkårsvurderingsid = vilkarsvurderingsid?.let(UUID::fromString) ?: UUID.randomUUID(),
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = maVurderesManuelt
)

private fun Vilkarsvurdering.toFrontendParagraf11_6() = FrontendParagraf_11_6(
    vilkårsvurderingsid = vilkarsvurderingsid?.let(UUID::fromString) ?: UUID.randomUUID(),
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = maVurderesManuelt
)

private fun Vilkarsvurdering.toFrontendParagraf11_12() = FrontendParagraf_11_12(
    vilkårsvurderingsid = vilkarsvurderingsid?.let(UUID::fromString) ?: UUID.randomUUID(),
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = maVurderesManuelt
)

private fun Vilkarsvurdering.toFrontendParagraf11_29() = FrontendParagraf_11_29(
    vilkårsvurderingsid = vilkarsvurderingsid?.let(UUID::fromString) ?: UUID.randomUUID(),
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
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
