package oppgavestyring.oppgave.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.authToken
import oppgavestyring.oppgave.adapter.*

fun Route.oppgaver(oppgaveClient: OppgaveClient) {

    route("/oppgaver") {

        get {
//            val token = call.authToken()
//                ?: return@get call.respond(HttpStatusCode.Unauthorized)

            oppgaveClient.søk(
                token = Token("venter på wonderwall i frontend"),
                params = SøkQueryParams(
                    tema = listOf("AAP"),
                    statuskategori = Statuskategori.AAPEN,
                ),
            ).onSuccess {
                call.respond(HttpStatusCode.OK, map(it))
            }.onFailure {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

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

private fun map(it: SøkOppgaverResponse): OppgaverResponse {
    return OppgaverResponse(
        it.oppgaver.map {
            Oppgave(
                oppgaveId = it.id,
                oppgavetype = Oppgavetype.AVKLARINGSBEHOV,
                foedselsnummer = it.aktoerId!!,
                opprettet = it.opprettetTidspunkt!!
            )
        }
    )
}