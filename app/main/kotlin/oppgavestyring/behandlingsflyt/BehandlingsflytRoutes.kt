package oppgavestyring.behandlingsflyt

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.SECURE_LOG
import oppgavestyring.authToken
import oppgavestyring.oppgave.Personident
import oppgavestyring.oppgave.OppgaveService

fun Route.behandlingsflyt(oppgaveService: OppgaveService) {
    route("/behandling") {
        post {
            val req = call.receive<Request>()
            val token = call.authToken() ?: return@post call.respond(HttpStatusCode.Unauthorized)

            oppgaveService.opprett_v2(
                token = token,
                personident = Personident(req.personident),
                beskrivelse = "Test"//req.avklaringsbehov
            ).onSuccess { nyOppgave ->
                call.respond(HttpStatusCode.Created,"OK")
            }.onFailure {
                SECURE_LOG.warn("Feil fra oppgave", it)
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}