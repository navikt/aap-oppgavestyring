package no.nav.aap.app.frontendView

import no.nav.aap.app.kafka.*
import no.nav.aap.app.modell.Paragraf
import java.time.Year

internal fun SøkereKafkaDto.toFrontendView(): FrontendSøker =
    FrontendSøker(
        personident = personident,
        fødselsdato = fødselsdato,
        sak = saker.first().toFrontendView(),
        skjermet = false // TODO: Hent fra avro Soker
    )

private fun Sak.toFrontendView(): FrontendSak = FrontendSak(
    saksid = saksid,
    søknadstidspunkt = søknadstidspunkt,
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
    this.first { it.aktiv }.vilkårsvurderinger.firstOrNull { it.paragraf == paragrafnavn }

private fun Vilkårsvurdering.toFrontendParagraf11_2() = FrontendParagraf_11_2(
    vilkårsvurderingsid = vilkårsvurderingsid,
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = måVurderesManuelt
)

private fun Vilkårsvurdering.toFrontendParagraf11_3() = FrontendParagraf_11_3(
    vilkårsvurderingsid = vilkårsvurderingsid,
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = måVurderesManuelt
)

private fun Vilkårsvurdering.toFrontendParagraf11_4() = FrontendParagraf_11_4(
    vilkårsvurderingsid = vilkårsvurderingsid,
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = måVurderesManuelt
)

private fun Vilkårsvurdering.toFrontendParagraf11_5() = FrontendParagraf_11_5(
    vilkårsvurderingsid = vilkårsvurderingsid,
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = måVurderesManuelt
)

private fun Vilkårsvurdering.toFrontendParagraf11_6() = FrontendParagraf_11_6(
    vilkårsvurderingsid = vilkårsvurderingsid,
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = måVurderesManuelt
)

private fun Vilkårsvurdering.toFrontendParagraf11_12() = FrontendParagraf_11_12(
    vilkårsvurderingsid = vilkårsvurderingsid,
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = måVurderesManuelt
)

private fun Vilkårsvurdering.toFrontendParagraf11_29() = FrontendParagraf_11_29(
    vilkårsvurderingsid = vilkårsvurderingsid,
    erOppfylt = tilstand in listOf("OPPFYLT", "OPPFYLT_MASKINELT"), // TODO
    måVurderesManuelt = måVurderesManuelt
)

private fun Vedtak.toFrontendView() = FrontendVedtak(
    vedtaksid = vedtaksid,
    innvilget = innvilget,
    inntektsgrunnlag = inntektsgrunnlag.toFrontendView(),
    vedtaksdato = vedtaksdato,
    virkningsdato = virkningsdato
)

private fun Inntektsgrunnlag.toFrontendView() = FrontendInntektsgrunnlag(
    beregningsdato = beregningsdato,
    inntekterSiste3Kalenderår = inntekterSiste3Kalenderår.map { it.toFrontendView() },
    fødselsdato = fødselsdato,
    sisteKalenderår = Year.from(sisteKalenderår),
    grunnlagsfaktor = grunnlagsfaktor,
)

private fun InntekterForBeregning.toFrontendView() = FrontendInntektsgrunnlagForÅr(
    inntekter = inntekter.map { it.toFrontendView() },
    år = inntektsgrunnlagForÅr.år,
    beløpFørJustering = inntektsgrunnlagForÅr.beløpFørJustering,
    beløpJustertFor6G = inntektsgrunnlagForÅr.beløpJustertFor6G,
    erBeløpJustertFor6G = inntektsgrunnlagForÅr.erBeløpJustertFor6G,
    grunnlagsfaktor = inntektsgrunnlagForÅr.grunnlagsfaktor,
)

private fun Inntekt.toFrontendView() = FrontendInntekt(
    arbeidsgiver = arbeidsgiver,
    inntekstmåned = inntekstmåned,
    beløp = beløp
)
