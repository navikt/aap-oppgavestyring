package oppgavestyring.oppgave.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.LOG
import oppgavestyring.oppgave.NavIdent
import oppgavestyring.oppgave.OppgaveService
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject


fun Route.oppgaver() {

    val oppgaveService: OppgaveService by inject()

    route("/oppgaver") {

        get {
            LOG.info("Forsøker å søke opp alle oppgaver tilknyttet AAP")

            val searchParams = parseUrlFiltering(call.request.queryParameters)
            val oppgaver = transaction {
                val oppgaver = if (!searchParams.isEmpty())
                    oppgaveService.søk(searchParams)
                else
                    oppgaveService.hentÅpneOppgaver()

                oppgaver.map { OppgaveDto.fromOppgave(it) }
            }
            call.respond(OppgaverResponse(oppgaver))
        }

        get("/{id}") {
            LOG.info("Forsøker å hente én konkret oppgave")

            val id = call.parameters["id"]?.toLong()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "mangler path-param 'id'")

            val oppgave = transaction {
                val oppgave = oppgaveService.hent(
                    oppgaveId = id
                )
                OppgaveDto.fromOppgave(oppgave)
            }
            call.respond(HttpStatusCode.OK, oppgave)
        }

        patch("/{id}/tildelRessurs") {
            LOG.info("Forsøker å tildele ressurs til oppgave")

            val id = call.parameters["id"]?.toLong()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "mangler path-param 'id'")

            val tildelRessursRequest = call.receive<TildelRessursRequest>()

            transaction {
                oppgaveService.tildelOppgave(
                    id = id,
                    navIdent = NavIdent(tildelRessursRequest.navIdent)
                )
            }

            call.respond(HttpStatusCode.OK)
        }

        patch("/{id}/frigi") {
            LOG.info("Forsøker å frigi ressurs fra oppgave")

            LOG.info("Token OK")

            val id = call.parameters["id"]?.toLong()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "mangler path-param 'id'")

            LOG.info("Uthenting av ID OK")

            transaction {
                oppgaveService.frigiRessursFraOppgave(
                    id = id,
                )
            }

            call.respond(HttpStatusCode.OK)

        }
    }
}

