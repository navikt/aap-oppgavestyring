package no.nav.aap.app

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.server.testing.*
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.frontendView.FrontendSak
import no.nav.aap.app.frontendView.FrontendSakstype
import no.nav.aap.app.frontendView.FrontendSøker
import no.nav.aap.app.frontendView.FrontendVilkårsvurdering
import no.nav.aap.avro.manuell.v1.Manuell
import no.nav.aap.avro.sokere.v1.*
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables
import java.time.LocalDate
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
                            "f422222c-8606-4426-b929-c2b8b4417367",
                            listOf(
                                Sakstype(
                                    "STANDARD",
                                    true,
                                    listOf(
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417301",
                                            paragraf = "PARAGRAF_11_2",
                                            ledd = listOf("LEDD_1", "LEDD_2"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417302",
                                            paragraf = "PARAGRAF_11_3",
                                            ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417303",
                                            paragraf = "PARAGRAF_11_4",
                                            ledd = listOf("LEDD_1"),
                                            tilstand = "OPPFYLT",
                                            måVurderesManuelt = false
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417304",
                                            paragraf = "PARAGRAF_11_4",
                                            ledd = listOf("LEDD_2", "LEDD_3"),
                                            tilstand = "IKKE_RELEVANT",
                                            måVurderesManuelt = false
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417305",
                                            paragraf = "PARAGRAF_11_5",
                                            ledd = listOf("LEDD_1", "LEDD_2"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417306",
                                            paragraf = "PARAGRAF_11_6",
                                            ledd = listOf("LEDD_1"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417307",
                                            paragraf = "PARAGRAF_11_12",
                                            ledd = listOf("LEDD_1"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417308",
                                            paragraf = "PARAGRAF_11_29",
                                            ledd = listOf("LEDD_1"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        )
                                    )
                                )
                            ),
                            LocalDate.of(2022, 1, 1),
                            VurderingAvBeregningsdato("SØKNAD_MOTTATT", null),
                            LocalDate.of(2022, 1, 1).atStartOfDay(),
                            "SØKNAD_MOTTATT",
                            null
                        )
                    )
                )
            }

            val saker = getSaker(mocks, "/api/sak")
            val expected = listOf(
                FrontendSøker(
                    personident = "12345678910",
                    fødselsdato = LocalDate.of(1990, 1, 1),
                    sak = FrontendSak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        tilstand = "SØKNAD_MOTTATT",
                        sakstype = FrontendSakstype(
                            type = "STANDARD",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417301"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1", "LEDD_2"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417302"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417303"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "OPPFYLT",
                                    måVurderesManuelt = false
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417304"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_2", "LEDD_3"),
                                    tilstand = "IKKE_RELEVANT",
                                    måVurderesManuelt = false
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417305"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1", "LEDD_2"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417306"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417307"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417308"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                )
                            )
                        ),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        vedtak = null
                    )
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
                            "f422222c-8606-4426-b929-c2b8b4417367",
                            listOf(
                                Sakstype(
                                    "STANDARD",
                                    true,
                                    listOf(
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417301",
                                            paragraf = "PARAGRAF_11_2",
                                            ledd = listOf("LEDD_1", "LEDD_2"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417302",
                                            paragraf = "PARAGRAF_11_3",
                                            ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417303",
                                            paragraf = "PARAGRAF_11_4",
                                            ledd = listOf("LEDD_1"),
                                            tilstand = "OPPFYLT",
                                            måVurderesManuelt = false
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417304",
                                            paragraf = "PARAGRAF_11_4",
                                            ledd = listOf("LEDD_2", "LEDD_3"),
                                            tilstand = "IKKE_RELEVANT",
                                            måVurderesManuelt = false
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417305",
                                            paragraf = "PARAGRAF_11_5",
                                            ledd = listOf("LEDD_1", "LEDD_2"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417306",
                                            paragraf = "PARAGRAF_11_6",
                                            ledd = listOf("LEDD_1"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417307",
                                            paragraf = "PARAGRAF_11_12",
                                            ledd = listOf("LEDD_1"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        ),
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417308",
                                            paragraf = "PARAGRAF_11_29",
                                            ledd = listOf("LEDD_1"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        )
                                    )
                                )
                            ),
                            LocalDate.of(2022, 1, 1),
                            VurderingAvBeregningsdato("SØKNAD_MOTTATT", null),
                            LocalDate.of(2022, 1, 1).atStartOfDay(),
                            "SØKNAD_MOTTATT",
                            null
                        )
                    )
                )
            }

            val saker = getSaker(mocks, "/api/sak/12345678910")
            val expected = listOf(
                FrontendSøker(
                    personident = "12345678910",
                    fødselsdato = LocalDate.of(1990, 1, 1),
                    sak = FrontendSak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        tilstand = "SØKNAD_MOTTATT",
                        sakstype = FrontendSakstype(
                            type = "STANDARD",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417301"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1", "LEDD_2"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417302"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417303"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "OPPFYLT",
                                    måVurderesManuelt = false
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417304"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_2", "LEDD_3"),
                                    tilstand = "IKKE_RELEVANT",
                                    måVurderesManuelt = false
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417305"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1", "LEDD_2"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417306"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417307"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                ),
                                FrontendVilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417308"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                )
                            )
                        ),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        vedtak = null
                    )
                )
            )

            assertEquals(expected, saker)
        }
    }

    @Test
    fun `Slett søker ved tombstone`() {
        withTestApp { mocks ->
            søkerTopic.produce("12345678910") {
                Soker(
                    "12345678910",
                    LocalDate.of(1990, 1, 1),
                    listOf(
                        Sak(
                            "f422222c-8606-4426-b929-c2b8b4417367",
                            listOf(
                                Sakstype(
                                    "STANDARD",
                                    true,
                                    listOf(
                                        vilkarsvurdering(
                                            vilkårsvurderingsid = UUID.randomUUID().toString(),
                                            paragraf = "PARAGRAF_11_2",
                                            ledd = listOf("LEDD_1", "LEDD_2"),
                                            tilstand = "SØKNAD_MOTTATT",
                                            måVurderesManuelt = true
                                        )
                                    )
                                )
                            ),
                            LocalDate.of(2022, 1, 1),
                            VurderingAvBeregningsdato("SØKNAD_MOTTATT", null),
                            LocalDate.of(2022, 1, 1).atStartOfDay(),
                            "SØKNAD_MOTTATT",
                            null
                        )
                    )
                )
            }
            assertEquals(1, getSaker(mocks, "/api/sak").size)

            søkerTopic.produceTombstone("12345678910")
            assertEquals(0, getSaker(mocks, "/api/sak").size)

            assertEquals(0, rowCount(mocks, "soker"))
            assertEquals(0, rowCount(mocks, "sak"))
            assertEquals(0, rowCount(mocks, "oppgave"))
            assertEquals(0, rowCount(mocks, "rolle"))
        }
    }

    private fun rowCount(mocks: Mocks, tabell: String): Int {
        @Language("PostgreSQL")
        val query = """
            SELECT count(1) FROM $tabell          
        """
        return sessionOf(initDatasource(mocks.postgres)).use { session ->
            session.run(queryOf(query).map { row -> row.int(1) }.asSingle)!!
        }
    }

    private fun initDatasource(postgreSQLContainer: PostgreSQLContainer<Nothing>) =
        HikariDataSource(HikariConfig().apply {
            jdbcUrl = postgreSQLContainer.jdbcUrl
            username = postgreSQLContainer.username
            password = postgreSQLContainer.password
            maximumPoolSize = 3
            minimumIdle = 1
            initializationFailTimeout = 5000
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        })

    private fun vilkarsvurdering(
        vilkårsvurderingsid: String,
        paragraf: String,
        ledd: List<String>,
        tilstand: String,
        måVurderesManuelt: Boolean,
        losning_11_2_manuell: Losning_11_2? = null,
        losning_11_2_maskinell: Losning_11_2? = null,
        losning_11_3_manuell: Losning_11_3? = null,
        losning_11_4_l2_l3_manuell: Losning_11_4_l2_l3? = null,
        losning_11_5_manuell: Losning_11_5? = null,
        losning_11_6_manuell: Losning_11_6? = null,
        losning_11_12_l1_manuell: Losning_11_12_l1? = null,
        losning_11_29_manuell: Losning_11_29? = null
    ) = Vilkarsvurdering(
        vilkårsvurderingsid,
        paragraf,
        ledd,
        tilstand,
        måVurderesManuelt,
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

    private fun TestApplicationEngine.getSaker(mocks: Mocks, path: String): List<FrontendSøker> {
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
        }

        inline fun <reified T> TestApplicationResponse.parseBody(): T = objectMapper.readValue(content!!)

        private val objectMapper = jacksonObjectMapper().apply { registerModule(JavaTimeModule()) }

        private lateinit var søkerTopic: TestInputTopic<String, Soker>
        private lateinit var manuellOutputTopic: TestOutputTopic<String, Manuell>
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
                "DB_HOST" to mocks.postgres.host,
                "DB_PORT" to mocks.postgres.firstMappedPort.toString(),
                "DB_DATABASE" to mocks.postgres.databaseName,
                "DB_USERNAME" to mocks.postgres.username,
                "DB_PASSWORD" to mocks.postgres.password
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
