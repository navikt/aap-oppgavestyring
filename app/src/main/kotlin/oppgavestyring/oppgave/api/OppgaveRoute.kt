package oppgavestyring.oppgave.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.LOG
import oppgavestyring.authToken
import oppgavestyring.oppgave.NavIdent
import oppgavestyring.oppgave.OppgaveService
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.oppgaver(oppgaveService: OppgaveService) {

    route("/oppgaver") {

        get {
            LOG.info("Forsøker å søke opp alle oppgaver tilknyttet AAP")
            transaction {
                oppgaveService.søk()
            }
        }

        get("/{id}") {
            LOG.info("Forsøker å hente én konkret oppgave")

            val id = call.parameters["id"]?.toLong()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "mangler path-param 'id'")

            val oppgave = transaction {
                val oppgave = oppgaveService.hent(
                    oppgaveId = id
                )
                Oppgave(
                    oppgaveId = oppgave.id.value,
                    avklaringsbehov = oppgave.avklaringsbehovtype,
                    foedselsnummer = oppgave.personnummer,
                    status = oppgave.status,
                    avklaringsbehovOpprettetTid = oppgave.avklaringsbehovOpprettetTidspunkt,
                    behandlingOpprettetTid = oppgave.behandlingOpprettetTidspunkt,
                    tilordnetRessurs = oppgave.tildelt?.ident,
                )
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
        }

        patch("/{id}/frigi") {
            LOG.info("Forsøker å frigi ressurs fra oppgave")

            val token = call.authToken()
                ?: return@patch call.respond(HttpStatusCode.Unauthorized)

            LOG.info("Token OK")

            val id = call.parameters["id"]?.toLong()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "mangler path-param 'id'")

            LOG.info("Uthenting av ID OK")

            val frigiOppgaveRequest = call.receive<FrigiOppgaveRequest>()

            transaction {
                oppgaveService.frigiRessursFraOppgave(
                    id = id,
                )
            }
        }
    }
}

