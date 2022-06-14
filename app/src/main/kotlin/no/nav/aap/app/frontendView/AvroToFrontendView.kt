package no.nav.aap.app.frontendView

import no.nav.aap.app.axsys.InnloggetBruker
import no.nav.aap.app.kafka.*
import no.nav.aap.app.modell.Paragraf
import java.time.Year

internal fun SøkereKafkaDto.toFrontendView(innloggetBruker: InnloggetBruker): FrontendSøker =
    FrontendSøker(
        personident = personident,
        fødselsdato = fødselsdato,
        sak = saker.first().toFrontendView(innloggetBruker),
        skjermet = false // TODO: Hent fra avro Soker
    )

private fun Sak.toFrontendView(innloggetBruker: InnloggetBruker): FrontendSak = FrontendSak(
    saksid = saksid,
    søknadstidspunkt = søknadstidspunkt,
    type = sakstyper.first { it.aktiv }.type,
    vedtak = vedtak?.toFrontendView(),
    paragraf_11_2 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_2.name)?.toFrontendParagraf11_2(innloggetBruker),
    paragraf_11_3 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_3.name)?.toFrontendParagraf11_3(innloggetBruker),
    paragraf_11_4 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_4.name)?.toFrontendParagraf11_4(innloggetBruker),
    paragraf_11_5 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_5.name)?.toFrontendParagraf11_5(innloggetBruker),
    paragraf_11_6 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_6.name)?.toFrontendParagraf11_6(innloggetBruker),
    paragraf_11_12 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_12.name)?.toFrontendParagraf11_12(innloggetBruker),
    paragraf_11_29 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_29.name)?.toFrontendParagraf11_29(innloggetBruker)
)

private fun Iterable<Sakstype>.finnVilkårsvurdering(paragrafnavn: String) =
    this.first { it.aktiv }.vilkårsvurderinger.firstOrNull { it.paragraf == paragrafnavn }

private fun Vilkårsvurdering.toFrontendParagraf11_2(innloggetBruker: InnloggetBruker) = FrontendParagraf_11_2(
    vilkårsvurderingsid = vilkårsvurderingsid,
    utfall = utfall.name,
    autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this)
)

private fun Vilkårsvurdering.toFrontendParagraf11_3(innloggetBruker: InnloggetBruker) = FrontendParagraf_11_3(
    vilkårsvurderingsid = vilkårsvurderingsid,
    utfall = utfall.name,
    autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this)
)

private fun Vilkårsvurdering.toFrontendParagraf11_4(innloggetBruker: InnloggetBruker) = FrontendParagraf_11_4(
    vilkårsvurderingsid = vilkårsvurderingsid,
    utfall = utfall.name,
    autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this)
)

private fun Vilkårsvurdering.toFrontendParagraf11_5(innloggetBruker: InnloggetBruker) = FrontendParagraf_11_5(
    vilkårsvurderingsid = vilkårsvurderingsid,
    utfall = utfall.name,
    autorisasjon = innloggetBruker.hentAutorisasjonForLokalkontor(this),
    kravOmNedsattArbeidsevneErOppfylt = løsning_11_5_manuell?.kravOmNedsattArbeidsevneErOppfylt,
    nedsettelseSkyldesSykdomEllerSkade = løsning_11_5_manuell?.nedsettelseSkyldesSykdomEllerSkade
)

private fun Vilkårsvurdering.toFrontendParagraf11_6(innloggetBruker: InnloggetBruker) = FrontendParagraf_11_6(
    vilkårsvurderingsid = vilkårsvurderingsid,
    utfall = utfall.name,
    autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this),
    harBehovForBehandling = løsning_11_6_manuell?.harBehovForBehandling,
    harBehovForTiltak = løsning_11_6_manuell?.harBehovForTiltak,
    harMulighetForÅKommeIArbeid = løsning_11_6_manuell?.harMulighetForÅKommeIArbeid
)

private fun Vilkårsvurdering.toFrontendParagraf11_12(innloggetBruker: InnloggetBruker) = FrontendParagraf_11_12(
    vilkårsvurderingsid = vilkårsvurderingsid,
    utfall = utfall.name,
    autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this),
    bestemmesAv = løsning_11_12_ledd1_manuell?.bestemmesAv,
    unntak = løsning_11_12_ledd1_manuell?.unntak,
    unntaksbegrunnelse = løsning_11_12_ledd1_manuell?.unntaksbegrunnelse,
    manueltSattVirkningsdato = løsning_11_12_ledd1_manuell?.manueltSattVirkningsdato
)

private fun Vilkårsvurdering.toFrontendParagraf11_29(innloggetBruker: InnloggetBruker) = FrontendParagraf_11_29(
    vilkårsvurderingsid = vilkårsvurderingsid,
    utfall = utfall.name,
    autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this)
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
    inntektsmåned = inntekstmåned,
    beløp = beløp
)
