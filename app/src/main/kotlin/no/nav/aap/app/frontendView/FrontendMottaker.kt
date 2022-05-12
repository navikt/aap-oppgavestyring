package no.nav.aap.app.frontendView

import java.time.LocalDate
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

class FrontendOppdrag { //FIXME Kan endres til data class når den får innhold
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode() = javaClass.hashCode()
}
