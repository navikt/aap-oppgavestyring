package no.nav.aap.app.kafka

import java.time.LocalDate
import java.util.*

data class DtoMottaker(
    val personident: String,
    val fødselsdato: LocalDate,
    val vedtakshistorikk: List<DtoVedtak>,
    val aktivitetstidslinje: List<DtoMeldeperiode>,
    val utbetalingstidslinjehistorikk: List<DtoUtbetalingstidslinje>,
    val oppdragshistorikk: List<DtoOppdrag>,
    val tilstand: String
)

data class DtoVedtak(
    val vedtaksid: UUID,
    val innvilget: Boolean,
    val grunnlagsfaktor: Double,
    val vedtaksdato: LocalDate,
    val virkningsdato: LocalDate,
    val fødselsdato: LocalDate
)

data class DtoMeldeperiode(
    val dager: List<DtoDag>
)

data class DtoDag(
    val dato: LocalDate,
    val arbeidstimer: Double?,
    val type: String
)

data class DtoUtbetalingstidslinje(
    val dager: List<DtoUtbetalingstidslinjedag>
)

data class DtoUtbetalingstidslinjedag(
    val dato: LocalDate,
    val grunnlagsfaktor: Double?,
    val barnetillegg: Double?,
    val grunnlag: Double?,
    val dagsats: Double?,
    val høyestebeløpMedBarnetillegg: Double?,
    val beløpMedBarnetillegg: Double?,
    val beløp: Double?,
    val arbeidsprosent: Double
)

class DtoOppdrag
