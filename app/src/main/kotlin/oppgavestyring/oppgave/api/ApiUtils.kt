package oppgavestyring.oppgave.api

import io.ktor.http.*
import io.ktor.util.*
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.oppgave.db.OppgaveTabell
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


enum class SearchParams {
    sortering,
    filtrering
}

data class OppgaveParams(
    val filters: Map<String, List<String>>,
    val sorting: Map<String, SortOrder>
) {
  fun isEmpty() = filters.isEmpty() && sorting.isEmpty()
}

fun parseUrlFiltering(parameters: Parameters) = OppgaveParams(
    sorting = parseSorting(parameters.getAll(SearchParams.sortering.name) ?: emptyList()),
    filters = parseFilters(parameters.getAll(SearchParams.filtrering.name) ?: emptyList()),
)

fun parseFilters(filters: List<String>) =
    filters.flatMap { parseQueryString(it).flattenEntries() }
        .fold(mutableMapOf<String, List<String>>()) { acc, value ->
            acc[value.first] = acc[value.first]?.plus(value.second) ?: mutableListOf(value.second)
            acc
        }

fun parseSorting(sortings: List<String>) =
    sortings.flatMap { parseQueryString(it).entries() }
        .associate { it.key to SortOrder.valueOf(it.value.first().uppercase()) }

fun generateOppgaveFilter(searchParams: OppgaveParams) = searchParams.filters.map { filter ->
    when (filter.key) {
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
        OppgaveDto::behandlingstype.name -> OppgaveTabell.behandlingstype to it.value
        OppgaveDto::avklaringsbehov.name -> OppgaveTabell.avklaringbehovtype to it.value
        OppgaveDto::avklaringsbehovOpprettetTid.name -> OppgaveTabell.avklaringsbehovOpprettetTidspunkt to it.value
        OppgaveDto::behandlingOpprettetTid.name -> OppgaveTabell.behandlingOpprettetTidspunkt to it.value
        else -> null
    }
}.filterNotNull().toTypedArray()