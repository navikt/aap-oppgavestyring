package no.nav.aap.app.frontendView

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class FrontendMottaker(
    val personident: String,
    val fødselsdato: LocalDate,
    val vedtakshistorikk: List<FrontendMottakerVedtak>,
    val aktivitetstidslinje: List<FrontendMeldeperiode>,
    val utbetalingstidslinjehistorikk: List<FrontendUtbetalingstidslinje>,
    val oppdragshistorikk: List<FrontendOppdrag>
)

data class FrontendMottakerVedtak(
    val vedtaksid: UUID,
    val innvilget: Boolean,
    val grunnlagsfaktor: Double,
    val vedtaksdato: LocalDate,
    val virkningsdato: LocalDate,
    val fødselsdato: LocalDate
)

data class FrontendMeldeperiode(
    val dager: List<FrontendDag>
)

data class FrontendDag(
    val dato: LocalDate,
    val arbeidstimer: Double?,
    val type: String
)

data class FrontendUtbetalingstidslinje(
    val dager: List<FrontendUtbetalingstidslinjedag>
)

data class FrontendUtbetalingstidslinjedag(
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

data class FrontendOppdrag(
    val mottaker: String,
    val fagområde: String,
    val linjer: List<FrontendUtbetalingslinje>,
    val fagsystemId: String,
    val endringskode: String,
    val nettoBeløp: Int,
    val overføringstidspunkt: LocalDateTime?,
    val avstemmingsnøkkel: Long?,
    val status: String?,
    val tidsstempel: LocalDateTime
)

data class FrontendUtbetalingslinje(
    val fom: LocalDate,
    val tom: LocalDate,
    val satstype: String,
    val beløp: Int?,
    val aktuellDagsinntekt: Int?,
    val grad: Int?,
    val refFagsystemId: String?,
    val delytelseId: Int,
    val refDelytelseId: Int?,
    val endringskode: String,
    val klassekode: String,
    val datoStatusFom: LocalDate?
)
