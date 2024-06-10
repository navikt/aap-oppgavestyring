package oppgavestyring.actuators.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

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
