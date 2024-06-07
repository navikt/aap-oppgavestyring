package oppgavestyring.filter

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.LOG
import oppgavestyring.config.security.OppgavePrincipal
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.filter() {

    route("filter") {
        get {
            val filter = transaction {
                OppgaveFilter.all()
                    .map { FilterDto.fromBusinessObject(it) }
            }
            call.respond(filter)
        }

        post {
            val filter = call.receive<FilterDto>()
            transaction {
                OppgaveFilter.new {
                    tittel = filter.tittel
                    beskrivelse = filter.beskrivelse
                    this.filter = filter.filter
                    opprettetAv = call.principal<OppgavePrincipal>()!!.ident.asString()
                }
            }
        }

        delete("/{id}") {
            val filterId = call.parameters["id"]?.toLong() ?: throw IllegalArgumentException("Filterid mangler")
            val principal = call.principal<OppgavePrincipal>()!!
            LOG.info("Bruker: ${principal.ident} sletter filter: $filterId")

            transaction {
                OppgaveFilterTable.deleteWhere { OppgaveFilterTable.id eq filterId }
            }

            call.respond(HttpStatusCode.OK)
        }
    }

}