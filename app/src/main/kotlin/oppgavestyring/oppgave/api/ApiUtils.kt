package oppgavestyring.oppgave.api

import io.ktor.http.*
import io.ktor.util.*
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import java.lang.reflect.TypeVariable
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties


@Target(AnnotationTarget.PROPERTY)
annotation class Sortable

@Target(AnnotationTarget.PROPERTY)
annotation class Filterable

enum class SearchParams {
    sortering,
    filtrering
}

data class OppgaveParams<T: Table>(
    val filters: Map<KProperty1<T, Column<Any>>, List<Any>>,
    val sorting: Map<KProperty1<T, Column<Any>>, SortOrder>
) {
  fun isEmpty() = filters.isEmpty() && sorting.isEmpty()
}

inline fun <reified T: Table>parseUrlFiltering(parameters: Parameters) = OppgaveParams(
    sorting = parseSorting<T>(parameters.getAll("sortering") ?: emptyList()),
    filters = parseFilters<T>(parameters.getAll(SearchParams.filtrering.name) ?: emptyList()),
)

inline fun <reified T: Table>parseFilters(filters: List<String>) =
    filters.flatMap { parseQueryString(it).flattenEntries() }
        .fold(mutableMapOf<String, List<String>>()) { acc, value ->
            acc[value.first] = acc[value.first]?.plus(value.second) ?: mutableListOf(value.second)
            acc
        }
        .filter { it.key in getAnnotatedMembers<T, Filterable>() }
        .mapKeys { property -> T::class.memberProperties.find { it.name == property.key }!! as KProperty1<T, Column<Any>> }

inline fun <reified T: Table>parseSorting(sortings: List<String>) =
    sortings.flatMap { parseQueryString(it).entries() }
        .filter { it.key in getAnnotatedMembers<T, Sortable>() }
        .associate { it.key to SortOrder.valueOf(it.value.first().uppercase()) }
        .mapKeys { property -> T::class.memberProperties.find { it.name == property.key }!! as KProperty1<T, Column<Any>> }

inline fun <reified T: Table, reified A: Annotation>getAnnotatedMembers() = T::class.memberProperties.filter { it.annotations.any{ it is A}}.map { it.name }.toList()