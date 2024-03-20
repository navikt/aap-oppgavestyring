package oppgavestyring.fakes

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.port
import oppgavestyring.proxy.OpprettRequest
import oppgavestyring.proxy.OpprettResponse
import oppgavestyring.proxy.Status


class OppgaveFake : AutoCloseable {
    var oppgaveIdSeq = 0L
        private set

    private val nextId: Long get() = ++oppgaveIdSeq
    private val server = embeddedServer(Netty, port = 0, module = { oppgave { nextId } }).apply { start() }
    val port: Int = server.port()
    override fun close() = server.stop(0, 0)
}

private fun Application.oppgave(nextSequence: () -> Long) {
    val oppgaver = mutableMapOf<Long, OpprettRequest>()

    install(ContentNegotiation) {
        jackson {}
    }

    routing {
        get("/api/v1/oppgaver/{id}") {
            call.request.headers["X-Correlation-ID"]
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val id = call.parameters["id"]?.toLong()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            when (val oppgave = oppgaver[id]) {
                null -> call.respond(HttpStatusCode.NotFound)
                else -> call.respond(response(id, oppgave))
            }
        }

        post("/api/v1/oppgaver") {
            call.request.headers["X-Correlation-ID"]
                ?: return@post call.respond(HttpStatusCode.BadRequest)

            val oppgave: OpprettRequest = call.receive()
            val id = nextSequence()
            oppgaver[id] = oppgave

            call.response.header(HttpHeaders.Location, "/api/v1/oppgaver/$id")

            call.respond(
                HttpStatusCode.Created,
                response(id, oppgave)
            )
        }
    }
}

private fun response(id: Long, req: OpprettRequest): OpprettResponse {
    return OpprettResponse(
        id = id,
        tildeltEnhetsnr = req.tildeltEnhetsnr ?: "1234",
        tema = req.tema,
        oppgavetype = req.oppgavetype,
        versjon = 1,
        prioritet = req.prioritet,
        status = Status.OPPRETTET,
        aktivDato = req.aktivDato
    )
}