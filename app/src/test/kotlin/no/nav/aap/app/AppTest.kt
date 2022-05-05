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
import no.nav.aap.app.frontendView.*
import no.nav.aap.app.kafka.*
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
        withTestApp { mocks ->
            mocks.kafka.inputTopic(Topics.søkere)
            mocks.kafka.outputTopic(Topics.manuell)

            val request = handleRequest(HttpMethod.Get, "/actuator/live")
            assertEquals(HttpStatusCode.OK, request.response.status())
        }
    }

    @Test
    fun `is ready`() {
        withTestApp { mocks ->
            mocks.kafka.inputTopic(Topics.søkere)
            mocks.kafka.outputTopic(Topics.manuell)

            val request = handleRequest(HttpMethod.Get, "/actuator/ready")
            assertEquals(HttpStatusCode.OK, request.response.status())
        }
    }

    @Test
    fun metrics() {
        withTestApp { mocks ->
            mocks.kafka.inputTopic(Topics.søkere)
            mocks.kafka.outputTopic(Topics.manuell)

            val request = handleRequest(HttpMethod.Get, "/actuator/metrics")
            assertEquals(HttpStatusCode.OK, request.response.status())
        }
    }

    @Test
    fun `Authentisering av endepunkt for sending av løsning`() {
        withTestApp { mocks ->
            mocks.kafka.inputTopic(Topics.søkere)
            mocks.kafka.outputTopic(Topics.manuell)

            postLøsning(mocks, """{"løsning_11_3_manuell":{"erOppfylt":true}}""")
        }
    }

    @Test
    fun `Henter alle saker`() {
        withTestApp { mocks ->
            val søkerTopic = mocks.kafka.inputTopic(Topics.søkere)
            mocks.kafka.outputTopic(Topics.manuell)

            søkerTopic.produce("12345678910") {
                SøkereKafkaDto(
                    "12345678910",
                    LocalDate.of(1990, 1, 1),
                    listOf(
                        Sak(
                            saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                            sakstyper = listOf(
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
                            vurderingsdato = LocalDate.of(2022, 1, 1),
                            vurderingAvBeregningsdato = VurderingAvBeregningsdato("SØKNAD_MOTTATT", null),
                            søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                            tilstand = "SØKNAD_MOTTATT",
                            vedtak = null
                        )
                    )
                )
            }

            val saker = getSaker(mocks, "/api/sak")
            val expected = listOf(
                FrontendSøker(
                    personident = "12345678910",
                    fødselsdato = LocalDate.of(1990, 1, 1),
                    skjermet = false,
                    sak = FrontendSak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        type = "STANDARD",
                        paragraf_11_2 = FrontendParagraf_11_2(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417301"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
                        paragraf_11_3 = FrontendParagraf_11_3(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417302"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
                        paragraf_11_4 = FrontendParagraf_11_4(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417303"),
                            erOppfylt = true,
                            måVurderesManuelt = false
                        ),
                        paragraf_11_5 = FrontendParagraf_11_5(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417305"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
                        paragraf_11_6 = FrontendParagraf_11_6(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417306"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
                        paragraf_11_12 = FrontendParagraf_11_12(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417307"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
                        paragraf_11_29 = FrontendParagraf_11_29(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417308"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
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
            val søkerTopic = mocks.kafka.inputTopic(Topics.søkere)
            mocks.kafka.outputTopic(Topics.manuell)

            søkerTopic.produce("12345678910") {
                SøkereKafkaDto(
                    personident = "12345678910",
                    fødselsdato = LocalDate.of(1990, 1, 1),
                    saker = listOf(
                        Sak(
                            saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                            sakstyper = listOf(
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
                            vurderingsdato = LocalDate.of(2022, 1, 1),
                            vurderingAvBeregningsdato = VurderingAvBeregningsdato("SØKNAD_MOTTATT", null),
                            søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                            tilstand = "SØKNAD_MOTTATT",
                            vedtak = null
                        )
                    )
                )
            }

            val saker = getSaker(mocks, "/api/sak/12345678910")
            val expected = listOf(
                FrontendSøker(
                    personident = "12345678910",
                    fødselsdato = LocalDate.of(1990, 1, 1),
                    skjermet = false,
                    sak = FrontendSak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        type = "STANDARD",
                        paragraf_11_2 = FrontendParagraf_11_2(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417301"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
                        paragraf_11_3 = FrontendParagraf_11_3(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417302"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
                        paragraf_11_4 = FrontendParagraf_11_4(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417303"),
                            erOppfylt = true,
                            måVurderesManuelt = false
                        ),
                        paragraf_11_5 = FrontendParagraf_11_5(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417305"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
                        paragraf_11_6 = FrontendParagraf_11_6(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417306"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
                        paragraf_11_12 = FrontendParagraf_11_12(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417307"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
                        paragraf_11_29 = FrontendParagraf_11_29(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417308"),
                            erOppfylt = false,
                            måVurderesManuelt = true
                        ),
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
            val søkerTopic = mocks.kafka.inputTopic(Topics.søkere)
            mocks.kafka.outputTopic(Topics.manuell)

            søkerTopic.produce("12345678910") {
                SøkereKafkaDto(
                    "12345678910",
                    LocalDate.of(1990, 1, 1),
                    listOf(
                        Sak(
                            UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                            sakstyper = listOf(
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
                            vurderingsdato = LocalDate.of(2022, 1, 1),
                            vurderingAvBeregningsdato = VurderingAvBeregningsdato("SØKNAD_MOTTATT", null),
                            søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                            tilstand = "SØKNAD_MOTTATT",
                            vedtak = null
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
        løsningYrkesskade_manuell: LøsningManuellMedlemskapYrkesskade? = null,
        løsningYrkesskade_maskinell: LøsningMaskinellMedlemskapYrkesskade? = null,
        losning_11_2_manuell: LøsningParagraf_11_2? = null,
        losning_11_2_maskinell: LøsningParagraf_11_2? = null,
        losning_11_3_manuell: LøsningParagraf_11_3? = null,
        losning_11_4_l2_l3_manuell: LøsningParagraf_11_4_ledd2_ledd3? = null,
        losning_11_5_manuell: LøsningParagraf_11_5? = null,
        losning_11_6_manuell: LøsningParagraf_11_6? = null,
        losning_11_12_l1_manuell: LøsningParagraf_11_12_ledd1? = null,
        losning_11_29_manuell: LøsningParagraf_11_29? = null
    ) = Vilkårsvurdering(
        vilkårsvurderingsid = UUID.fromString(vilkårsvurderingsid),
        paragraf = paragraf,
        ledd = ledd,
        tilstand = tilstand,
        måVurderesManuelt = måVurderesManuelt,
        løsning_medlemskap_yrkesskade_maskinell = løsningYrkesskade_maskinell,
        løsning_medlemskap_yrkesskade_manuell = løsningYrkesskade_manuell,
        løsning_11_2_maskinell = losning_11_2_maskinell,
        løsning_11_2_manuell = losning_11_2_manuell,
        løsning_11_3_manuell = losning_11_3_manuell,
        løsning_11_4_ledd2_ledd3_manuell = losning_11_4_l2_l3_manuell,
        løsning_11_5_manuell = losning_11_5_manuell,
        løsning_11_5_yrkesskade_manuell = null,
        løsning_11_6_manuell = losning_11_6_manuell,
        løsning_11_12_ledd1_manuell = losning_11_12_l1_manuell,
        løsning_11_22_manuell = null,
        løsning_11_29_manuell = losning_11_29_manuell,
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
        inline fun <reified T> TestApplicationResponse.parseBody(): T = objectMapper.readValue(content!!)

        private val objectMapper = jacksonObjectMapper().apply { registerModule(JavaTimeModule()) }
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
                "KAFKA_SCHEMA_REGISTRY" to "mock://schema-registry",
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
                    test(mocks)
                }
            }
        }
    }
}
