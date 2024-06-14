package oppgavestyring.intern.filter

import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.delete
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import io.ktor.http.*
import io.ktor.server.auth.*
import oppgavestyring.LOG
import oppgavestyring.config.security.OppgavePrincipal
import oppgavestyring.respondWithStatus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

data class SlettFilterRequest(@PathParam(description = "ID to delete.") val id: Long?)

fun NormalOpenAPIRoute.filter() {

    route("/filter") {

        post<Unit, FilterDto, FilterDto> { _, body ->
            val principal = pipeline.context.authentication.principal<OppgavePrincipal>()!!.ident.toString()
            transaction {
                OppgaveFilter.new {
                    tittel = body.tittel
                    beskrivelse = body.beskrivelse
                    this.filter = body.filter
                    opprettetAv = principal
                }
            }
            respondWithStatus(HttpStatusCode.Created, body)
        }

        get<Unit, List<FilterDto>> {
            val filter = transaction {
                OppgaveFilter.all()
                    .map { FilterDto.fromBusinessObject(it) }
            }
            respond(filter)
        }

        route("/{id}") {
            delete<SlettFilterRequest, String> { req ->
                val filterId = req.id ?: throw IllegalArgumentException("Filter-id mangler")
                val principal = pipeline.context.authentication.principal<OppgavePrincipal>()!!

                LOG.info("Bruker: ${principal.ident} sletter filter: $filterId")

                transaction {
                    OppgaveFilterTable.deleteWhere { OppgaveFilterTable.id eq filterId }
                }

                respondWithStatus(HttpStatusCode.OK, "{}")
            }
        }
    }
}