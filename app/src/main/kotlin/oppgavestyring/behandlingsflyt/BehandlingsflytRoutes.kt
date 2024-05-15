package oppgavestyring.behandlingsflyt

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.LOG
import oppgavestyring.behandlingsflyt.dto.BehandlingshistorikkRequest
import org.jetbrains.exposed.sql.transactions.transaction

fun Route.behandlingsflyt(behandlingsflytAdapter: BehandlingsflytAdapter) {
    route("/behandling") {
        post {
            LOG.info("Mottok saksendring på behandling")
            val req = call.receive<BehandlingshistorikkRequest>()
            transaction {
                behandlingsflytAdapter.mapBehnadlingshistorikkTilOppgaveHendelser(req)
            }

            call.respond(HttpStatusCode.Created)
        }
    }
}
