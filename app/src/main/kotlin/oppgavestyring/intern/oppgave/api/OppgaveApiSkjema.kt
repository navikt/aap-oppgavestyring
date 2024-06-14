package oppgavestyring.intern.oppgave.api

import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.parameters.QueryParamStyle

enum class SortOrder {
    asc, desc;

    fun toSQLSortOrder(): org.jetbrains.exposed.sql.SortOrder {
        return org.jetbrains.exposed.sql.SortOrder.valueOf(this.name.uppercase())
    }
}

data class OppgaverByIdRequest(@PathParam(description = "Hent oppgave med ID.") val id: Long?)

data class ListOppgaverRequest(
    @QueryParam(
        description = "What keys to sort on and in what order.",
        style = QueryParamStyle.deepObject
    ) val sortering: Map<String, SortOrder>
)
