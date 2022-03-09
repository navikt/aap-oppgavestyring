package no.nav.aap.app

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.aap.app.frontendView.FrontendSak
import no.nav.aap.app.frontendView.FrontendSakstype
import no.nav.aap.app.frontendView.FrontendVilkårsvurdering
import no.nav.aap.avro.manuell.v1.Manuell
import no.nav.aap.avro.sokere.v1.*
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
                    "12345678910",
                    LocalDate.of(1990, 1, 1),
                    listOf(
                        Sak(
                            listOf(
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1", "LEDD_2"),
                                    tilstand = "SØKNAD_MOTTATT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                    tilstand = "SØKNAD_MOTTATT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "OPPFYLT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_2", "LEDD_3"),
                                    tilstand = "IKKE_RELEVANT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1", "LEDD_2"),
                                    tilstand = "SØKNAD_MOTTATT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT"
                                )
                            ),
                            LocalDate.of(2022, 1, 1),
                            VurderingAvBeregningsdato("SØKNAD_MOTTATT", null),
                            "SØKNAD_MOTTATT",
                            null
                        )
                    )
                )
            }

            val saker = getSaker(mocks, "/api/sak")
            val expected = listOf(
                FrontendSak(
                    personident = "12345678910",
                    fødselsdato = LocalDate.of(1990, 1, 1),
                    tilstand = "SØKNAD_MOTTATT",
                    sakstype = FrontendSakstype(
                        type = "STANDARD",
                        vilkårsvurderinger = listOf(
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_2",
                                ledd = listOf("LEDD_1", "LEDD_2"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_3",
                                ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_4",
                                ledd = listOf("LEDD_1"),
                                tilstand = "OPPFYLT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_4",
                                ledd = listOf("LEDD_2", "LEDD_3"),
                                tilstand = "IKKE_RELEVANT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_5",
                                ledd = listOf("LEDD_1", "LEDD_2"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_6",
                                ledd = listOf("LEDD_1"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_12",
                                ledd = listOf("LEDD_1"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_29",
                                ledd = listOf("LEDD_1"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            )
                        )
                    ),
                    vedtak = null
                )
            )
            assertEquals(expected, saker)
        }
    }

    @Test
    fun `Henter alle saker til en søker`() {
        withTestApp { mocks ->
            søkerTopic.produce("12345678910") {
                Soker(
                    "12345678910",
                    LocalDate.of(1990, 1, 1),
                    listOf(
                        Sak(
                            listOf(
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1", "LEDD_2"),
                                    tilstand = "SØKNAD_MOTTATT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                    tilstand = "SØKNAD_MOTTATT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "OPPFYLT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_2", "LEDD_3"),
                                    tilstand = "IKKE_RELEVANT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1", "LEDD_2"),
                                    tilstand = "SØKNAD_MOTTATT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT"
                                ),
                                vilkarsvurdering(
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT"
                                )
                            ),
                            LocalDate.of(2022, 1, 1),
                            VurderingAvBeregningsdato("SØKNAD_MOTTATT", null),
                            "SØKNAD_MOTTATT",
                            null
                        )
                    )
                )
            }

            val saker = getSaker(mocks, "/api/sak/12345678910")
            val expected = listOf(
                FrontendSak(
                    personident = "12345678910",
                    fødselsdato = LocalDate.of(1990, 1, 1),
                    tilstand = "SØKNAD_MOTTATT",
                    sakstype = FrontendSakstype(
                        type = "STANDARD",
                        vilkårsvurderinger = listOf(
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_2",
                                ledd = listOf("LEDD_1", "LEDD_2"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_3",
                                ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_4",
                                ledd = listOf("LEDD_1"),
                                tilstand = "OPPFYLT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_4",
                                ledd = listOf("LEDD_2", "LEDD_3"),
                                tilstand = "IKKE_RELEVANT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_5",
                                ledd = listOf("LEDD_1", "LEDD_2"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_6",
                                ledd = listOf("LEDD_1"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_12",
                                ledd = listOf("LEDD_1"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            ),
                            FrontendVilkårsvurdering(
                                paragraf = "PARAGRAF_11_29",
                                ledd = listOf("LEDD_1"),
                                tilstand = "SØKNAD_MOTTATT",
                                harÅpenOppgave = false
                            )
                        )
                    ),
                    vedtak = null
                )
            )
            assertEquals(expected, saker)
        }
    }

    private fun vilkarsvurdering(
        paragraf: String,
        ledd: List<String>,
        tilstand: String,
        losning_11_2_manuell: Losning_11_2? = null,
        losning_11_2_maskinell: Losning_11_2? = null,
        losning_11_3_manuell: Losning_11_3? = null,
        losning_11_4_l2_l3_manuell: Losning_11_4_l2_l3? = null,
        losning_11_5_manuell: Losning_11_5? = null,
        losning_11_6_manuell: Losning_11_6? = null,
        losning_11_12_l1_manuell: Losning_11_12_l1? = null,
        losning_11_29_manuell: Losning_11_29? = null
    ) = Vilkarsvurdering(
        paragraf,
        ledd,
        tilstand,
        losning_11_2_manuell,
        losning_11_2_maskinell,
        losning_11_3_manuell,
        losning_11_4_l2_l3_manuell,
        losning_11_5_manuell,
        losning_11_6_manuell,
        losning_11_12_l1_manuell,
        losning_11_29_manuell
    )


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
