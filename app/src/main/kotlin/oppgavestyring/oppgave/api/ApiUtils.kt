package oppgavestyring.oppgave.api

import io.ktor.http.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.SortOrder


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

