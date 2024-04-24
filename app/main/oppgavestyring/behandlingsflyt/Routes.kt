package oppgavestyring.behandlingsflyt

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.SECURE_LOG
import oppgavestyring.authToken
import oppgavestyring.oppgave.OppgaveRepository
import oppgavestyring.oppgave.OppgaveService
import oppgavestyring.oppgave.adapter.OpprettRequest
import oppgavestyring.oppgave.adapter.Prioritet
import java.time.LocalDate

fun Route.behandlingsflyt(oppgaveService: OppgaveService, repository: OppgaveRepository) {
    route("/behandling") {
        post {
            val req = call.receive<Request>()
            val token = call.authToken() ?: return@post call.respond(HttpStatusCode.Unauthorized)
            val oppgave = OpprettRequest(
                oppgavetype = "BEH_SAK",
                prioritet = Prioritet.NORM,
                aktivDato = LocalDate.now().toString(),
                personident = req.personident,
                beskrivelse = req.beskrivelse,
                opprettetAvEnhetsnr = "9999",
                behandlesAvApplikasjon = "KELVIN"
            )
            oppgaveService.opprett(
                token,
                oppgave
            ).onSuccess { nyOppgave ->
                repository.lagre(nyOppgave)
                call.respond("OK")
            }.onFailure {
                SECURE_LOG.warn("Feil fra oppgave", it)
                call.respond(HttpStatusCode.InternalServerError)
            }


        }
    }
}