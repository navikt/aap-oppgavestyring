package no.nav.aap.app

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables

internal class AppTest {

    @Test
    fun `is alive`() {
        withTestApp {
            val request = handleRequest(HttpMethod.Get, "/actuator/live")
            assertEquals(HttpStatusCode.OK, request.response.status())
        }
    }

    @Test
    fun `is ready`() {
        withTestApp {
            val request = handleRequest(HttpMethod.Get, "/actuator/ready")
            assertEquals(HttpStatusCode.OK, request.response.status())
        }
    }

    @Test
    fun metrics() {
        withTestApp {
            val request = handleRequest(HttpMethod.Get, "/actuator/metrics")
            assertEquals(HttpStatusCode.OK, request.response.status())
        }
    }

    @Test
    fun `Authentisering av endepunkt for sending av løsning`() {
        withTestApp { mocks ->
            val request = handleRequest(HttpMethod.Post, "/api/sak/12345678910/losning") {
                val token = mocks.azure.issueAzureToken()
                addHeader("Authorization", "Bearer ${token.serialize()}")
            }
            assertEquals(HttpStatusCode.OK, request.response.status())
        }
    }

private fun withTestApp(test: TestApplicationEngine.(mocks: Mocks) -> Unit) {
    Mocks().use { mocks ->
        val externalConfig = mapOf(
            "AZURE_OPENID_CONFIG_ISSUER" to "azure",
            "AZURE_APP_WELL_KNOWN_URL" to mocks.azure.wellKnownUrl(),
            "AZURE_APP_CLIENT_ID" to "oppgavestyring",
        )

        EnvironmentVariables(externalConfig).execute<Unit> {
            withTestApplication(Application::server) { test(mocks) }
        }
    }
}
}
