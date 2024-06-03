package oppgavestyring.oppgave.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.LOG
import oppgavestyring.config.NAV_IDENT_CLAIM_NAME
import oppgavestyring.oppgave.NavIdent
import oppgavestyring.oppgave.OppgaveService
import org.jetbrains.exposed.sql.transactions.transaction


fun Route.oppgaver(oppgaveService: OppgaveService ) {

    route("/oppgaver") {

        get {
            LOG.info("Forsøker å søke opp alle oppgaver tilknyttet AAP")
            val ident = NavIdent(call.authentication.principal<JWTPrincipal>()?.getClaim(NAV_IDENT_CLAIM_NAME, String::class)!!)

            val searchParams = parseUrlFiltering(call.request.queryParameters)
            val oppgaver = transaction {
                val oppgaver = if (!searchParams.isEmpty())
                    oppgaveService.søk(ident, searchParams)
                else
                    oppgaveService.hentÅpneOppgaver(ident)

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

            call.respond(HttpStatusCode.NoContent)
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

            call.respond(HttpStatusCode.NoContent)

        }
    }
}

