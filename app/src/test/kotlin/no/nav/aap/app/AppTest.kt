package no.nav.aap.app

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
            postLøsning(mocks, """{"løsning_11_3_manuell":{"erOppfylt":true}}""")
        }
    }

    private fun TestApplicationEngine.postLøsning(mocks: Mocks, body: String) {
        val request = handleRequest(HttpMethod.Post, "/api/sak/123/losning") {
            val token = mocks.azure.issueAzureToken()
            addHeader("Authorization", "Bearer ${token.serialize()}")
            addHeader(HttpHeaders.ContentType, "application/json")
            setBody(body)
        }
        assertEquals(request.response.status(), HttpStatusCode.OK)
    }

    private fun withTestApp(test: TestApplicationEngine.(mocks: Mocks) -> Unit) {
    Mocks().use { mocks ->
        val externalConfig = mapOf(
            "AZURE_OPENID_CONFIG_ISSUER" to "azure",
            "AZURE_APP_WELL_KNOWN_URL" to mocks.azure.wellKnownUrl(),
            "AZURE_APP_CLIENT_ID" to "oppgavestyring",
            "KAFKA_BROKERS" to "mock://kafka",
            "KAFKA_TRUSTSTORE_PATH" to "",
            "KAFKA_SECURITY_ENABLED" to "false",
            "KAFKA_KEYSTORE_PATH" to "",
            "KAFKA_CREDSTORE_PASSWORD" to "",
            "KAFKA_CLIENT_ID" to "oppgavestyring",
            "KAFKA_GROUP_ID" to "oppgavestyring-1",
            "KAFKA_SCHEMA_REGISTRY" to mocks.kafka.schemaRegistryUrl,
            "KAFKA_SCHEMA_REGISTRY_USER" to "",
            "KAFKA_SCHEMA_REGISTRY_PASSWORD" to "",
        )

        EnvironmentVariables(externalConfig).execute<Unit> {
            withTestApplication({ server(KafkaSetupMock()) }) { test(mocks) }
        }
    }
}
}
