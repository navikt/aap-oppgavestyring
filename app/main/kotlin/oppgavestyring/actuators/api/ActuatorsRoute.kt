package oppgavestyring.actuators.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Route.actuators(prometheus: PrometheusMeterRegistry) {

    route("/actuator") {
        get("/live") {
            call.respond(HttpStatusCode.OK, "live")
        }
        get("/ready") {
            call.respond(HttpStatusCode.OK, "live")
        }
        get("/metrics") {
            call.respond(HttpStatusCode.OK, prometheus.scrape())
        }
    }

}