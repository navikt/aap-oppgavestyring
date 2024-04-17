package oppgavestyring.oppgave.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.authToken
import oppgavestyring.oppgave.*
import oppgavestyring.oppgave.adapter.*

fun Route.oppgaver(oppgaveRepository: OppgaveRepository, oppgaveClient: OppgaveClient) {

    val oppgaveService = OppgaveService(oppgaveRepository, oppgaveClient)

    route("/oppgaver") {

        get {
//            val token = call.authToken()
//                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            oppgaveService.søk(
                Token("venter på wonderwall i frontend")
            ).onSuccess {
                call.respond(HttpStatusCode.OK, map(it))
            }.onFailure {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        get("/{id}") {
            val token = call.authToken()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val id = call.parameters["id"]?.let { OppgaveId(it.toLong()) }
                ?: return@get call.respond(HttpStatusCode.BadRequest, "mangler path-param 'id'")

            oppgaveService.hent(
                token = token,
                oppgaveId = id
            ).onSuccess {
                call.respond(HttpStatusCode.OK, it)
            }.onFailure {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        post("/opprett") {
            val token = call.authToken()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            oppgaveService.opprett(
                token = token,
                request = call.receive()
            ).onSuccess {
                call.respond(HttpStatusCode.Created, it)
            }.onFailure {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        patch("/{id}/tildelRessurs") {
            val token = call.authToken()
                ?: return@patch call.respond(HttpStatusCode.Unauthorized)
            val id = call.parameters["id"]?.let { OppgaveId(it.toLong()) }
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "mangler path-param 'id'")

            val tildelRessursRequest = call.receive<TildelRessursRequest>()

            oppgaveService.tildelRessursTilOppgave(
                id = id,
                versjon = Versjon(tildelRessursRequest.versjon),
                navIdent = NavIdent(tildelRessursRequest.navIdent),
                token = token
            )
        }
    }
}

private fun map(it: SøkOppgaverResponse): OppgaverResponse {
    return OppgaverResponse(
        it.oppgaver.map {
            Oppgave(
                oppgaveId = it.id,
                versjon = it.versjon,
                oppgavetype = Oppgavetype.AVKLARINGSBEHOV,
                foedselsnummer = it.aktoerId!!, //TODO:
                tilordnetRessurs = it.tilordnetRessurs,
                opprettet = it.opprettetTidspunkt!!
            )
        }
    )
}