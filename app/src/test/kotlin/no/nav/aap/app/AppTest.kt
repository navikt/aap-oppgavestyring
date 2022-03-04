package no.nav.aap.app

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AppTest {

    @Test
    fun `is alive`() {
        withTestApplication(Application::server) {
            val request = handleRequest(HttpMethod.Get, "/actuator/live")
            assertEquals(HttpStatusCode.OK, request.response.status())
        }
    }

    @Test
    fun `is ready`() {
        withTestApplication(Application::server) {
            val request = handleRequest(HttpMethod.Get, "/actuator/ready")
            assertEquals(HttpStatusCode.OK, request.response.status())
        }
    }

    @Test
    fun metrics() {
        withTestApplication(Application::server) {
            val request = handleRequest(HttpMethod.Get, "/actuator/metrics")
            assertEquals(HttpStatusCode.OK, request.response.status())
        }
    }
}
