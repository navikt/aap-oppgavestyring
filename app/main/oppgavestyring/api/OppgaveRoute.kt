package oppgavestyring.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.authToken
import oppgavestyring.proxy.OppgaveClient

fun Route.proxy(oppgaveClient: OppgaveClient) {
    route("/oppgave") {

        get("/{id}") {
            val token = call.authToken()
                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            val id = call.parameters["id"]?.toLong()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "mangler path-param 'id'")

            oppgaveClient.hent(
                token = token,
                oppgaveId = id,
            ).onSuccess {
                call.respond(HttpStatusCode.OK, it)
            }.onFailure {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        post("/opprett") {
            val token = call.authToken()
                ?: return@post call.respond(HttpStatusCode.Unauthorized)

            oppgaveClient.opprett(
                token = token,
                request = call.receive()
            ).onSuccess {
                call.respond(HttpStatusCode.Created, it)
            }.onFailure {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}