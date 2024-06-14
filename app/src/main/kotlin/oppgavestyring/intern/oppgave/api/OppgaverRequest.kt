package oppgavestyring.intern.oppgave.api

import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.parameters.QueryParamStyle

enum class SortOrder {
    asc, desc;

    fun toSQLSortOrder(): org.jetbrains.exposed.sql.SortOrder {
        return org.jetbrains.exposed.sql.SortOrder.valueOf(this.name.uppercase())
    }
}


data class ListOppgaverRequest(
    @QueryParam(description = "xxxx", style = QueryParamStyle.deepObject) val sortering: Map<String, SortOrder>
) {
}
