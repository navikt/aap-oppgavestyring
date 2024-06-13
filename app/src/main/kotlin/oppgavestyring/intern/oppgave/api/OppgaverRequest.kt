package oppgavestyring.intern.oppgave.api

import com.papsign.ktor.openapigen.annotations.parameters.QueryParam
import com.papsign.ktor.openapigen.parameters.QueryParamStyle
import org.jetbrains.exposed.sql.SortOrder

data class ListOppgaverRequest(
    @QueryParam(description = "xxxx", style = QueryParamStyle.deepObject) val filters: Map<String, List<String>>,
    @QueryParam(description = "xxxx", style = QueryParamStyle.deepObject) val sorting: Map<String, SortOrder>
) {
}