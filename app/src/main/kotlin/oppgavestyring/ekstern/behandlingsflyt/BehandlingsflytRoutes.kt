package oppgavestyring.ekstern.behandlingsflyt

import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.route
import io.ktor.http.*
import oppgavestyring.LOG
import oppgavestyring.ekstern.behandlingsflyt.dto.BehandlingshistorikkRequest
import oppgavestyring.respondWithStatus
import org.jetbrains.exposed.sql.transactions.transaction

fun NormalOpenAPIRoute.behandlingsflyt(behandlingsflytAdapter: BehandlingsflytAdapter) {
    route("/behandling") {
        post<Unit, String, BehandlingshistorikkRequest> { _, req ->
            LOG.info("Mottok saksendring på behandling")

            transaction {
                behandlingsflytAdapter.mapBehandlingshistorikkTilOppgaveHendelser(req)
            }

            respondWithStatus(HttpStatusCode.Created, "{}")
        }
    }
}
