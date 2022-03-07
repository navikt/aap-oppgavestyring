package no.nav.aap.app

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.aap.app.frontendView.FrontendSak
import no.nav.aap.avro.manuell.v1.Manuell
import no.nav.aap.avro.sokere.v1.Sak
import no.nav.aap.avro.sokere.v1.Soker
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.apache.kafka.streams.state.KeyValueStore
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import java.time.LocalDate

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

    @Test
    fun `Henter alle saker`() {
        withTestApp { mocks ->
            søkerTopic.produce("12345678910") {
                Soker(
                    "12345678910", LocalDate.of(1956, 1, 12), listOf(
                        Sak(
                            emptyList(),
                            LocalDate.now(),
                            null,
                            "TILSTAND",
                            null
                        )
                    )
                )
            }

            val saker = getSaker(mocks, "/api/sak")

            assertEquals(1, saker.size)
        }
    }

    @Test
    fun `Henter alle saker til en søker`() {
        withTestApp { mocks ->
            søkerTopic.produce("12345678910") {
                Soker(
                    "12345678910", LocalDate.of(1956, 1, 12), listOf(
                        Sak(
                            emptyList(),
                            LocalDate.now(),
                            null,
                            "TILSTAND",
                            null
                        )
                    )
                )
            }

            val saker = getSaker(mocks, "/api/sak/12345678910")

            assertEquals(1, saker.size)
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

    private fun TestApplicationEngine.getSaker(mocks: Mocks, path: String): List<FrontendSak> {
        val request = handleRequest(HttpMethod.Get, path) {
            val token = mocks.azure.issueAzureToken()
            addHeader("Authorization", "Bearer ${token.serialize()}")
            addHeader(HttpHeaders.ContentType, "application/json")
        }
        assertEquals(request.response.status(), HttpStatusCode.OK)
        return request.response.parseBody()
    }

    companion object {
        internal fun initializeTopics(kafka: KafkaSetupMock) {
            søkerTopic = kafka.inputAvroTopic("aap.sokere.v1")
            manuellOutputTopic = kafka.outputAvroTopic("aap.manuell.v1")
            stateStore = kafka.getKeyValueStore("oppgavestyring-soker-state-store")
        }

        inline fun <reified T> TestApplicationResponse.parseBody(): T = objectMapper.readValue(content!!)

        private val objectMapper = jacksonObjectMapper().apply { registerModule(JavaTimeModule()) }

        private lateinit var søkerTopic: TestInputTopic<String, Soker>
        private lateinit var manuellOutputTopic: TestOutputTopic<String, Manuell>
        private lateinit var stateStore: KeyValueStore<String, Soker>
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
                withTestApplication({ server(mocks.kafka) }) {
                    initializeTopics(mocks.kafka)
                    test(mocks)
                }
            }
        }
    }
}
