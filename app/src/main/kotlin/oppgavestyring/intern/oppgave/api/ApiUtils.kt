package oppgavestyring.intern.oppgave.api

import io.ktor.http.*
import io.ktor.util.*
import oppgavestyring.ekstern.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.ekstern.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.intern.oppgave.db.OppgaveTabell
import oppgavestyring.intern.oppgave.db.TildeltTabell
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.collections.component1
import kotlin.collections.component2

data class OppgaveParams(
    val filters: Map<String, List<String>>,
    val sorting: Map<String, SortOrder>
) {
  fun isEmpty() = filters.isEmpty() && sorting.isEmpty()
}

const val filtrering = "filtrering"

fun trekkUtFilterParametere(parameters: Parameters) = parameters.toMap()
    .filter { it.key.startsWith(filtrering) }
    .mapKeys { it.key.removePrefix(filtrering+"[").removeSuffix("]").trim() }

fun generateOppgaveFilter(searchParams: OppgaveParams) = searchParams.filters.map { filter ->
    when (filter.key) {
        OppgaveDto::foedselsnummer.name -> OppgaveTabell.personnummer like "%${filter.value.first()}%"
        OppgaveDto::tilordnetRessurs.name -> TildeltTabell.ident like "%${filter.value.first()}%"
        OppgaveDto::behandlingstype.name -> OppgaveTabell.behandlingstype
            .inList(filter.value.map { Behandlingstype.valueOf(it) })
        OppgaveDto::avklaringsbehov.name -> OppgaveTabell.avklaringbehovtype
            .inList(filter.value.map { Avklaringsbehovtype.valueOf(it) })
        OppgaveDto::avklaringsbehovOpprettetTid.name -> timeWithinRange(OppgaveTabell.avklaringsbehovOpprettetTidspunkt, filter.value.first())
        OppgaveDto::behandlingOpprettetTid.name -> timeWithinRange(OppgaveTabell.behandlingOpprettetTidspunkt, filter.value.first())
        else -> null
    }
}.filterNotNull()
    .fold(Op.TRUE.and(Op.TRUE)) { acc, value -> acc.and(value) }

fun timeWithinRange(timeColumn: Column<LocalDateTime>, timeString: String): Op<Boolean> {
    val (fromTime, toTime) = getTimerangeFromISOStirng(timeString)
    val dateTimeFrom = fromTime.truncatedTo(ChronoUnit.DAYS)
    val dateTimeTo = toTime.truncatedTo(ChronoUnit.DAYS).plusDays(1)
    return timeColumn greaterEq dateTimeFrom and
            (OppgaveTabell.behandlingOpprettetTidspunkt lessEq dateTimeTo.plusDays(1))
}

fun getTimerangeFromISOStirng(dateRange: String): List<LocalDateTime> {
    val fromTimeToTime = dateRange.split("/").map { LocalDateTime.parse(it) }
    return if (fromTimeToTime.size == 1) {
        listOf(fromTimeToTime[0], fromTimeToTime[0])
    }
        else listOf(fromTimeToTime[0], fromTimeToTime[1])
}

fun generateOppgaveSorting(searchParams: OppgaveParams) = searchParams.sorting.map {
    when (it.key) {
        OppgaveDto::foedselsnummer.name -> OppgaveTabell.personnummer to it.value
        OppgaveDto::tilordnetRessurs.name -> TildeltTabell.ident to it.value
        OppgaveDto::behandlingstype.name -> OppgaveTabell.behandlingstype to it.value
        OppgaveDto::avklaringsbehov.name -> OppgaveTabell.avklaringbehovtype to it.value
        OppgaveDto::avklaringsbehovOpprettetTid.name -> OppgaveTabell.avklaringsbehovOpprettetTidspunkt to it.value
        OppgaveDto::behandlingOpprettetTid.name -> OppgaveTabell.behandlingOpprettetTidspunkt to it.value
        else -> null
    }
}.filterNotNull().toTypedArray()