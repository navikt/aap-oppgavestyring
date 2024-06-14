package oppgavestyring.testutils.fakes

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.ekstern.oppgaveapi.adapter.OpprettRequest
import oppgavestyring.ekstern.oppgaveapi.adapter.OpprettResponse
import oppgavestyring.ekstern.oppgaveapi.adapter.Status
import oppgavestyring.ekstern.oppslag.NavnDto
import oppgavestyring.testutils.port

class OppslagFake : AutoCloseable {
    private val server = embeddedServer(Netty, port = 0, module = { oppslag() }).apply { start() }
    val port: Int = server.port()
    override fun close() = server.stop(0, 0)
}

private fun Application.oppslag() {

    install(ContentNegotiation) {
        jackson {}
    }

    routing {
        get("/navn") {
            val personident = call.request.headers["personIdnet"]
                ?: throw IllegalArgumentException("personident mangler")

            call.respond(NavnDto("Testy", "Testersen"))
        }
    }
}
