package no.nav.aap.app.frontendView

import no.nav.aap.app.kafka.*

internal fun DtoMottaker.toFrontendView() = FrontendMottaker(
    personident = personident,
    fødselsdato = fødselsdato,
    vedtakshistorikk = vedtakshistorikk.toFrontendMottakerVedtak(),
    aktivitetstidslinje = aktivitetstidslinje.toFrontendMeldeperiode(),
    utbetalingstidslinjehistorikk = utbetalingstidslinjehistorikk.toFrontendUtbetalingstidslinje(),
    oppdragshistorikk = oppdragshistorikk.toFrontendOppdrag()
)

private fun Iterable<DtoVedtak>.toFrontendMottakerVedtak() = map {
    FrontendMottakerVedtak(
        vedtaksid = it.vedtaksid,
        innvilget = it.innvilget,
        grunnlagsfaktor = it.grunnlagsfaktor,
        vedtaksdato = it.vedtaksdato,
        virkningsdato = it.virkningsdato,
        fødselsdato = it.fødselsdato
    )
}

private fun Iterable<DtoMeldeperiode>.toFrontendMeldeperiode() = map {
    FrontendMeldeperiode(
        dager = it.dager.toFrontendDag()
    )
}

private fun Iterable<DtoDag>.toFrontendDag() = map {
    FrontendDag(
        dato = it.dato,
        arbeidstimer = it.arbeidstimer,
        type = it.type
    )
}

private fun Iterable<DtoUtbetalingstidslinje>.toFrontendUtbetalingstidslinje() = map {
    FrontendUtbetalingstidslinje(
        dager = it.dager.toFrontendUtbetalingstidslinjedag()
    )
}

private fun Iterable<DtoUtbetalingstidslinjedag>.toFrontendUtbetalingstidslinjedag() = map {
    FrontendUtbetalingstidslinjedag(
        dato = it.dato,
        grunnlagsfaktor = it.grunnlagsfaktor,
        barnetillegg = it.barnetillegg,
        grunnlag = it.grunnlag,
        dagsats = it.dagsats,
        høyestebeløpMedBarnetillegg = it.høyestebeløpMedBarnetillegg,
        beløpMedBarnetillegg = it.beløpMedBarnetillegg,
        beløp = it.beløp,
        arbeidsprosent = it.arbeidsprosent
    )
}

private fun Iterable<DtoOppdrag>.toFrontendOppdrag() = map { FrontendOppdrag() }
