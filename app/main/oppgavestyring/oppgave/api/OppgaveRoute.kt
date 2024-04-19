package oppgavestyring.oppgave.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.LOG
import oppgavestyring.SECURE_LOG
import oppgavestyring.authToken
import oppgavestyring.oppgave.*
import oppgavestyring.oppgave.adapter.OppgaveClient
import oppgavestyring.oppgave.adapter.SøkOppgaverResponse
import oppgavestyring.oppgave.adapter.Token
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.oppgaver(oppgaveRepository: OppgaveRepository, oppgaveClient: OppgaveClient) {

    val oppgaveService = OppgaveService(oppgaveRepository, oppgaveClient)

    route("/oppgaver") {

        get {
            LOG.info("Forsøker å søke opp alle oppgaver tilknyttet AAP")
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
            LOG.info("Forsøker å hente én konkret oppgave")

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
            LOG.info("Forsøker å opprette en ny oppgave")

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
            LOG.info("Forsøker å tildele ressurs til oppgave")

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
            ).onSuccess {
                call.respond(HttpStatusCode.OK, it)
            }.onFailure {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        patch("/{id}/frigi") {
            LOG.info("Forsøker å frigi ressurs fra oppgave")

            val token = call.authToken()
                ?: return@patch call.respond(HttpStatusCode.Unauthorized)

            LOG.info("Token OK")

            val id = call.parameters["id"]?.let { OppgaveId(it.toLong()) }
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "mangler path-param 'id'")

            LOG.info("Uthenting av ID OK")

            val tildelRessursRequest = call.receive<TildelRessursRequest>()

            oppgaveService.frigiRessursFraOppgave(
                id = id,
                versjon = Versjon(tildelRessursRequest.versjon),
                token = token
            ).onSuccess {
                LOG.info("Oppgave frigitt OK")
                call.respond(HttpStatusCode.OK, it)
            }.onFailure {
                LOG.error("Frigjøring av oppgave feilet: ${it.message}")
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
                versjon = it.versjon,
                oppgavetype = Oppgavetype.AVKLARINGSBEHOV,
                foedselsnummer = it.aktoerId!!, //TODO: Fødselsnummer, d-nummer eller aktørId?
                tilordnetRessurs = it.tilordnetRessurs,
                opprettet = it.opprettetTidspunkt!!
            )
        }
    )
}