package no.nav.aap.app.frontendView

import no.nav.aap.app.axsys.InnloggetBruker
import no.nav.aap.app.kafka.SøkereKafkaDto
import no.nav.aap.app.modell.Paragraf
import java.time.Year

internal fun SøkereKafkaDto.toFrontendView(innloggetBruker: InnloggetBruker): FrontendSøker =
    FrontendSøker(
        personident = personident,
        fødselsdato = fødselsdato,
        sak = saker.first().toFrontendView(innloggetBruker),
        skjermet = false // TODO: Hent fra avro Soker
    )

private fun SøkereKafkaDto.Sak.toFrontendView(innloggetBruker: InnloggetBruker): FrontendSak = FrontendSak(
    saksid = saksid,
    søknadstidspunkt = søknadstidspunkt,
    type = sakstyper.first { it.aktiv }.type,
    vedtak = vedtak?.toFrontendView(),
    inngangsvilkår = Inngangsvilkår(
        autorisasjon = sakstyper.finnVilkårsvurderinger(
            Paragraf.PARAGRAF_11_2.name,
            Paragraf.PARAGRAF_11_3.name,
            Paragraf.PARAGRAF_11_4.name
        ).hentAutorisasjonForNAY(innloggetBruker),
        paragraf_11_2 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_2.name)
            ?.toFrontendParagraf11_2(innloggetBruker),
        paragraf_11_3 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_3.name)
            ?.toFrontendParagraf11_3(innloggetBruker),
        paragraf_11_4 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_4.name)
            ?.toFrontendParagraf11_4(innloggetBruker),
    ),
    paragraf_11_2 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_2.name)
        ?.toFrontendParagraf11_2(innloggetBruker),
    paragraf_11_3 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_3.name)
        ?.toFrontendParagraf11_3(innloggetBruker),
    paragraf_11_4 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_4.name)
        ?.toFrontendParagraf11_4(innloggetBruker),
    paragraf_11_5 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_5.name)
        ?.toFrontendParagraf11_5(innloggetBruker),
    paragraf_11_6 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_6.name)
        ?.toFrontendParagraf11_6(innloggetBruker),
    paragraf_11_12 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_12.name)
        ?.toFrontendParagraf11_12(innloggetBruker),
    paragraf_11_19 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_19.name)
        ?.toFrontendParagraf11_19(innloggetBruker),
    beregningsdato = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_19.name)
        ?.vurderingAvBeregningsdatoToFrontendView(innloggetBruker),
    paragraf_11_29 = sakstyper.finnVilkårsvurdering(Paragraf.PARAGRAF_11_29.name)
        ?.toFrontendParagraf11_29(innloggetBruker),
)

private fun Iterable<SøkereKafkaDto.Vilkårsvurdering>.hentAutorisasjonForNAY(innloggetBruker: InnloggetBruker) =
    innloggetBruker.hentAutorisasjonForNAY(toList())

private fun Iterable<SøkereKafkaDto.Sakstype>.finnVilkårsvurdering(paragrafnavn: String) =
    this.single { it.aktiv }.vilkårsvurderinger.firstOrNull { it.paragraf == paragrafnavn }

private fun Iterable<SøkereKafkaDto.Sakstype>.finnVilkårsvurderinger(vararg paragrafnavn: String) =
    this.single { it.aktiv }.vilkårsvurderinger.filter { it.paragraf in paragrafnavn }

private fun SøkereKafkaDto.Vilkårsvurdering.toFrontendParagraf11_2(innloggetBruker: InnloggetBruker) =
    FrontendParagraf_11_2(
        vilkårsvurderingsid = vilkårsvurderingsid,
        utfall = utfall.name,
        autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this)
    )

private fun SøkereKafkaDto.Vilkårsvurdering.toFrontendParagraf11_3(innloggetBruker: InnloggetBruker) =
    FrontendParagraf_11_3(
        vilkårsvurderingsid = vilkårsvurderingsid,
        utfall = utfall.name,
        autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this)
    )

private fun SøkereKafkaDto.Vilkårsvurdering.toFrontendParagraf11_4(innloggetBruker: InnloggetBruker) =
    FrontendParagraf_11_4(
        vilkårsvurderingsid = vilkårsvurderingsid,
        utfall = utfall.name,
        autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this)
    )

private fun SøkereKafkaDto.Vilkårsvurdering.toFrontendParagraf11_5(innloggetBruker: InnloggetBruker) =
    FrontendParagraf_11_5(
        vilkårsvurderingsid = vilkårsvurderingsid,
        utfall = utfall.name,
        autorisasjon = innloggetBruker.hentAutorisasjonForLokalkontor(this),
        kravOmNedsattArbeidsevneErOppfylt = løsning_11_5_manuell?.lastOrNull()?.kravOmNedsattArbeidsevneErOppfylt,
        nedsettelseSkyldesSykdomEllerSkade = løsning_11_5_manuell?.lastOrNull()?.nedsettelseSkyldesSykdomEllerSkade
    )

private fun SøkereKafkaDto.Vilkårsvurdering.toFrontendParagraf11_6(innloggetBruker: InnloggetBruker) =
    FrontendParagraf_11_6(
        vilkårsvurderingsid = vilkårsvurderingsid,
        utfall = utfall.name,
        autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this),
        harBehovForBehandling = løsning_11_6_manuell?.lastOrNull()?.harBehovForBehandling,
        harBehovForTiltak = løsning_11_6_manuell?.lastOrNull()?.harBehovForTiltak,
        harMulighetForÅKommeIArbeid = løsning_11_6_manuell?.lastOrNull()?.harMulighetForÅKommeIArbeid
    )

private fun SøkereKafkaDto.Vilkårsvurdering.toFrontendParagraf11_12(innloggetBruker: InnloggetBruker) =
    FrontendParagraf_11_12(
        vilkårsvurderingsid = vilkårsvurderingsid,
        utfall = utfall.name,
        autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this),
        bestemmesAv = løsning_11_12_ledd1_manuell?.lastOrNull()?.bestemmesAv,
        unntak = løsning_11_12_ledd1_manuell?.lastOrNull()?.unntak,
        unntaksbegrunnelse = løsning_11_12_ledd1_manuell?.lastOrNull()?.unntaksbegrunnelse,
        manueltSattVirkningsdato = løsning_11_12_ledd1_manuell?.lastOrNull()?.manueltSattVirkningsdato
    )

private fun SøkereKafkaDto.Vilkårsvurdering.toFrontendParagraf11_29(innloggetBruker: InnloggetBruker) =
    FrontendParagraf_11_29(
        vilkårsvurderingsid = vilkårsvurderingsid,
        utfall = utfall.name,
        autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this)
    )

private fun SøkereKafkaDto.Vedtak.toFrontendView() = FrontendVedtak(
    vedtaksid = vedtaksid,
    innvilget = innvilget,
    inntektsgrunnlag = inntektsgrunnlag.toFrontendView(),
    vedtaksdato = vedtaksdato,
    virkningsdato = virkningsdato
)

private fun SøkereKafkaDto.Inntektsgrunnlag.toFrontendView() = FrontendInntektsgrunnlag(
    beregningsdato = beregningsdato,
    inntekterSiste3Kalenderår = inntekterSiste3Kalenderår.map { it.toFrontendView() },
    fødselsdato = fødselsdato,
    sisteKalenderår = Year.from(sisteKalenderår),
    grunnlagsfaktor = grunnlagsfaktor,
)

private fun SøkereKafkaDto.InntekterForBeregning.toFrontendView() = FrontendInntektsgrunnlagForÅr(
    inntekter = inntekter.map { it.toFrontendView() },
    år = inntektsgrunnlagForÅr.år,
    beløpFørJustering = inntektsgrunnlagForÅr.beløpFørJustering,
    beløpJustertFor6G = inntektsgrunnlagForÅr.beløpJustertFor6G,
    erBeløpJustertFor6G = inntektsgrunnlagForÅr.erBeløpJustertFor6G,
    grunnlagsfaktor = inntektsgrunnlagForÅr.grunnlagsfaktor,
)

private fun SøkereKafkaDto.Inntekt.toFrontendView() = FrontendInntekt(
    arbeidsgiver = arbeidsgiver,
    inntektsmåned = inntekstmåned,
    beløp = beløp
)

private fun SøkereKafkaDto.Vilkårsvurdering.toFrontendParagraf11_19(innloggetBruker: InnloggetBruker) =
    FrontendParagraf_11_19(
        vilkårsvurderingsid = vilkårsvurderingsid,
        utfall = utfall.name,
        autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this),
        beregningsdato = løsning_11_19_manuell?.lastOrNull()?.beregningsdato,
    )

@Deprecated("Erstattes av toFrontendParagraf11_19()", replaceWith = ReplaceWith("toFrontendParagraf11_19()"))
private fun SøkereKafkaDto.Vilkårsvurdering.vurderingAvBeregningsdatoToFrontendView(innloggetBruker: InnloggetBruker) =
    FrontendBeregningsdato(
        beregningsdato = løsning_11_19_manuell?.lastOrNull()?.beregningsdato,
        autorisasjon = innloggetBruker.hentAutorisasjonForNAY(this)
    )
