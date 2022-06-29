package no.nav.aap.app

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.nimbusds.jwt.SignedJWT
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.frontendView.*
import no.nav.aap.app.kafka.*
import no.nav.aap.app.security.JwtGenerator
import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.Topic
import no.nav.aap.kafka.streams.test.readAndAssert
import org.apache.kafka.streams.TestInputTopic
import org.apache.kafka.streams.TestOutputTopic
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.testcontainers.containers.PostgreSQLContainer
import java.time.LocalDate
import java.util.*
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AppTest {
    private lateinit var mocks: MockEnvironment
    private lateinit var søkerTopic: TestInputTopic<String, SøkereKafkaDto>
    private lateinit var mottakerTopic: TestInputTopic<String, DtoMottaker>
    private lateinit var løsning_11_2_manuell_OutputTopic: TestOutputTopic<String, Løsning_11_2_manuell>
    private lateinit var løsning_11_3_manuell_OutputTopic: TestOutputTopic<String, Løsning_11_3_manuell>
    private lateinit var løsning_11_4_ledd2_ledd3_manuell_OutputTopic: TestOutputTopic<String, Løsning_11_4_ledd2_ledd3_manuell>
    private lateinit var løsning_11_5_manuell_OutputTopic: TestOutputTopic<String, Løsning_11_5_manuell>
    private lateinit var løsning_11_6_manuell_OutputTopic: TestOutputTopic<String, Løsning_11_6_manuell>
    private lateinit var løsning_11_12_ledd1_manuell_OutputTopic: TestOutputTopic<String, Løsning_11_12_ledd1_manuell>
    private lateinit var løsning_11_19_manuell_OutputTopic: TestOutputTopic<String, Løsning_11_19_manuell>
    private lateinit var løsning_11_29_manuell_OutputTopic: TestOutputTopic<String, Løsning_11_29_manuell>

    @BeforeAll
    fun setupMockEnvironment() {
        mocks = MockEnvironment()
    }

    @AfterAll
    fun closeMockEnvironment() = mocks.close()

    @AfterEach
    fun assertNoUnassertedRecordsLeftOnTopic() {
        if (::løsning_11_2_manuell_OutputTopic.isInitialized) {
            løsning_11_2_manuell_OutputTopic.readAndAssert().isEmpty()
        }
        if (::løsning_11_3_manuell_OutputTopic.isInitialized) {
            løsning_11_3_manuell_OutputTopic.readAndAssert().isEmpty()
        }
        if (::løsning_11_4_ledd2_ledd3_manuell_OutputTopic.isInitialized) {
            løsning_11_4_ledd2_ledd3_manuell_OutputTopic.readAndAssert().isEmpty()
        }
        if (::løsning_11_5_manuell_OutputTopic.isInitialized) {
            løsning_11_5_manuell_OutputTopic.readAndAssert().isEmpty()
        }
        if (::løsning_11_6_manuell_OutputTopic.isInitialized) {
            løsning_11_6_manuell_OutputTopic.readAndAssert().isEmpty()
        }
        if (::løsning_11_12_ledd1_manuell_OutputTopic.isInitialized) {
            løsning_11_12_ledd1_manuell_OutputTopic.readAndAssert().isEmpty()
        }
        if (::løsning_11_29_manuell_OutputTopic.isInitialized) {
            løsning_11_29_manuell_OutputTopic.readAndAssert().isEmpty()
        }
        if (::løsning_11_19_manuell_OutputTopic.isInitialized) {
            løsning_11_19_manuell_OutputTopic.readAndAssert().isEmpty()
        }
    }

    @Test
    fun `actuators available without auth`() {
        testApplication {
            environment { config = mocks.applicationConfig() }
            application { server(mocks.kafka) }

            runBlocking {
                val live = client.get("actuator/live")
                assertEquals(HttpStatusCode.OK, live.status)

                val ready = client.get("actuator/ready")
                assertEquals(HttpStatusCode.OK, ready.status)

                val metrics = client.get("actuator/metrics")
                assertEquals(HttpStatusCode.OK, metrics.status)
                assertNotNull(metrics.bodyAsText())
            }
        }
    }

    @Test
    fun `Authentisering av endepunkt for 11-29 for sending av løsning som saksbehandler er ok`() {
        testApplication {
            environment { config = mocks.applicationConfig() }
            application { server(mocks.kafka) }

            val response = client.post("/api/sak/123/losning/paragraf_11_29") {
                bearerAuth(JwtGenerator.generateSaksbehandlerToken().serialize())
                contentType(ContentType.Application.Json)
                setBody("""{"erOppfylt":true}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `Authentisering av endepunkt for 11-29 for sending av løsning som veileder er ikke ok`() {
        testApplication {
            environment { config = mocks.applicationConfig() }
            application { server(mocks.kafka) }

            val response = client.post("/api/sak/123/losning/paragraf_11_29") {
                bearerAuth(JwtGenerator.generateVeilederToken().serialize())
                contentType(ContentType.Application.Json)
                setBody("""{"erOppfylt":true}""")
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    fun `Authentisering av endepunkt for 11-5 for sending av løsning som veileder er ok`() {
        testApplication {
            environment { config = mocks.applicationConfig() }
            application { server(mocks.kafka) }

            val response = client.post("/api/sak/123/losning/paragraf_11_5") {
                bearerAuth(JwtGenerator.generateVeilederToken().serialize())
                contentType(ContentType.Application.Json)
                setBody("""{"kravOmNedsattArbeidsevneErOppfylt":true, "nedsettelseSkyldesSykdomEllerSkade":true}""")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `Authentisering av endepunkt for 11-5 for sending av løsning som saksbehandler er ikke ok`() {
        testApplication {
            environment { config = mocks.applicationConfig() }
            application { server(mocks.kafka) }

            val response = client.post("/api/sak/123/losning/paragraf_11_5") {
                bearerAuth(JwtGenerator.generateSaksbehandlerToken().serialize())
                contentType(ContentType.Application.Json)
                setBody("""{"kravOmNedsattArbeidsevneErOppfylt":true, "nedsettelseSkyldesSykdomEllerSkade":true}""")
            }
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    fun `Sending av løsning legger på brukernavn fra token på kafkamelding`() {
        val app = TestApplication {
            environment { config = mocks.applicationConfig() }
            application {
                server(mocks.kafka)
            }
        }

        val client = app.createClient {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                }
            }
        }
        runBlocking { client.get("/actuator/live") }
        runBlocking {
            client.post("/api/sak/123/losning/paragraf_11_29") {
                bearerAuth(JwtGenerator.generateSaksbehandlerToken().serialize())
                contentType(ContentType.Application.Json)
                setBody(DtoLøsningParagraf_11_29(erOppfylt = true))
            }
        }

        val producer = mocks.kafka.getProducer(Topics.manuell_11_29)
        assertEquals("test.test@test.com", producer.history().single().value().vurdertAv)
    }

    @Test
    fun `Henter alle saker`() {
        lateinit var søkerTopic: TestInputTopic<String, SøkereKafkaDto>
        lateinit var personopplysningerTopic: TestInputTopic<String, PersonopplysningerKafkaDto>

        val app = TestApplication {
            environment { config = mocks.applicationConfig() }
            application {
                server(mocks.kafka)
                søkerTopic = mocks.kafka.inputTopic(Topic("aap.sokere.v1", JsonSerde.jackson()))
                personopplysningerTopic = mocks.kafka.inputTopic(Topics.personopplysninger)
            }
        }

        val client = app.createClient {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                }
            }
        }
        runBlocking { client.get("/actuator/live") }
        søkerTopic.produce("12345678910") {
            SøkereKafkaDto(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                saker = listOf(
                    SøkereKafkaDto.Sak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        sakstyper = listOf(
                            SøkereKafkaDto.Sakstype(
                                "STANDARD",
                                true,
                                listOf(
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417301",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_2",
                                        ledd = listOf("LEDD_1", "LEDD_2"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417302",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_3",
                                        ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417303",
                                        vurdertAv = "maskinell vurdering",
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_4",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "OPPFYLT",
                                        utfall = Utfall.OPPFYLT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417304",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_4",
                                        ledd = listOf("LEDD_2", "LEDD_3"),
                                        tilstand = "IKKE_RELEVANT",
                                        utfall = Utfall.IKKE_RELEVANT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417305",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_5",
                                        ledd = listOf("LEDD_1", "LEDD_2"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417306",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_6",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417307",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_12",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417308",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_19",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417309",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_29",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    )
                                )
                            )
                        ),
                        vurderingsdato = LocalDate.of(2022, 1, 1),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        tilstand = "SØKNAD_MOTTATT",
                        vedtak = null
                    )
                ),
                version = SøkereKafkaDto.VERSION
            )
        }

        personopplysningerTopic.produce("12345678910") {
            PersonopplysningerKafkaDto(
                norgEnhetId = "0001",
                adressebeskyttelse = "UGRADERT",
                geografiskTilknytning = "1234",
                skjerming = SkjermingKafkaDto(
                    erSkjermet = false,
                    fom = null,
                    tom = null
                )
            )
        }

        val saker = client.getSaker("/api/sak", JwtGenerator::generateSaksbehandlerToken)

        val expected = listOf(
            FrontendSøker(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                skjermet = false,
                sak = FrontendSak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    type = "STANDARD",
                    inngangsvilkår = Inngangsvilkår(
                        autorisasjon = Autorisasjon.ENDRE,
                        paragraf_11_2 = FrontendParagraf_11_2(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417301"),
                            utfall = "IKKE_VURDERT",
                            autorisasjon = Autorisasjon.ENDRE
                        ),
                        paragraf_11_3 = FrontendParagraf_11_3(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417302"),
                            utfall = "IKKE_VURDERT",
                            autorisasjon = Autorisasjon.ENDRE,
                        ),
                        paragraf_11_4 = FrontendParagraf_11_4(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417303"),
                            utfall = "OPPFYLT",
                            autorisasjon = Autorisasjon.ENDRE
                        ),
                    ),
                    paragraf_11_2 = FrontendParagraf_11_2(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417301"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE
                    ),
                    paragraf_11_3 = FrontendParagraf_11_3(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417302"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                    ),
                    paragraf_11_4 = FrontendParagraf_11_4(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417303"),
                        utfall = "OPPFYLT",
                        autorisasjon = Autorisasjon.ENDRE
                    ),
                    paragraf_11_5 = FrontendParagraf_11_5(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417305"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.LESE,
                        kravOmNedsattArbeidsevneErOppfylt = null,
                        nedsettelseSkyldesSykdomEllerSkade = null
                    ),
                    paragraf_11_6 = FrontendParagraf_11_6(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417306"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                        harBehovForBehandling = null,
                        harBehovForTiltak = null,
                        harMulighetForÅKommeIArbeid = null
                    ),
                    paragraf_11_12 = FrontendParagraf_11_12(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417307"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                        bestemmesAv = null,
                        unntak = null,
                        unntaksbegrunnelse = null,
                        manueltSattVirkningsdato = null
                    ),
                    paragraf_11_19 = FrontendParagraf_11_19(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417308"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                        beregningsdato = null,
                    ),
                    paragraf_11_29 = FrontendParagraf_11_29(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417309"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                    ),
                    beregningsdato = FrontendBeregningsdato(null, Autorisasjon.ENDRE),
                    vedtak = null
                )
            )
        )
        assertEquals(expected, saker)

        app.stop()
    }

    @Test
    fun `Henter alle saker til en søker`() {
        lateinit var personopplysningerTopic: TestInputTopic<String, PersonopplysningerKafkaDto>

        val app = TestApplication {
            environment { config = mocks.applicationConfig() }
            application {
                server(mocks.kafka)
                søkerTopic = mocks.kafka.inputTopic(Topic("aap.sokere.v1", JsonSerde.jackson()))
                personopplysningerTopic = mocks.kafka.inputTopic(Topics.personopplysninger)
            }
        }

        val client = app.createClient {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                }
            }
        }

        runBlocking { client.get("/actuator/live") }

        søkerTopic.produce("12345678910") {
            SøkereKafkaDto(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                saker = listOf(
                    SøkereKafkaDto.Sak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        sakstyper = listOf(
                            SøkereKafkaDto.Sakstype(
                                "STANDARD",
                                true,
                                listOf(
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417301",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_2",
                                        ledd = listOf("LEDD_1", "LEDD_2"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417302",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_3",
                                        ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417303",
                                        vurdertAv = "maskinell vurdering",
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_4",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "OPPFYLT",
                                        utfall = Utfall.OPPFYLT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417304",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_4",
                                        ledd = listOf("LEDD_2", "LEDD_3"),
                                        tilstand = "IKKE_RELEVANT",
                                        utfall = Utfall.IKKE_RELEVANT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417305",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_5",
                                        ledd = listOf("LEDD_1", "LEDD_2"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417306",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_6",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417307",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_12",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417308",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_19",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417309",
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_29",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    )
                                )
                            )
                        ),
                        vurderingsdato = LocalDate.of(2022, 1, 1),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        tilstand = "SØKNAD_MOTTATT",
                        vedtak = null
                    )
                ),
                version = SøkereKafkaDto.VERSION
            )
        }

        personopplysningerTopic.produce("12345678910") {
            PersonopplysningerKafkaDto(
                norgEnhetId = "0001",
                adressebeskyttelse = "UGRADERT",
                geografiskTilknytning = "1234",
                skjerming = SkjermingKafkaDto(
                    erSkjermet = false,
                    fom = null,
                    tom = null
                )
            )
        }

        val saker = client.getSaker("/api/sak/12345678910", JwtGenerator::generateSaksbehandlerToken)
        val expected = listOf(
            FrontendSøker(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                skjermet = false,
                sak = FrontendSak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    type = "STANDARD",
                    inngangsvilkår = Inngangsvilkår(
                        autorisasjon = Autorisasjon.ENDRE,
                        paragraf_11_2 = FrontendParagraf_11_2(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417301"),
                            utfall = "IKKE_VURDERT",
                            autorisasjon = Autorisasjon.ENDRE
                        ),
                        paragraf_11_3 = FrontendParagraf_11_3(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417302"),
                            utfall = "IKKE_VURDERT",
                            autorisasjon = Autorisasjon.ENDRE,
                        ),
                        paragraf_11_4 = FrontendParagraf_11_4(
                            vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417303"),
                            utfall = "OPPFYLT",
                            autorisasjon = Autorisasjon.ENDRE
                        ),
                    ),
                    paragraf_11_2 = FrontendParagraf_11_2(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417301"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE
                    ),
                    paragraf_11_3 = FrontendParagraf_11_3(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417302"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                    ),
                    paragraf_11_4 = FrontendParagraf_11_4(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417303"),
                        utfall = "OPPFYLT",
                        autorisasjon = Autorisasjon.ENDRE
                    ),
                    paragraf_11_5 = FrontendParagraf_11_5(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417305"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.LESE,
                        kravOmNedsattArbeidsevneErOppfylt = null,
                        nedsettelseSkyldesSykdomEllerSkade = null
                    ),
                    paragraf_11_6 = FrontendParagraf_11_6(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417306"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                        harBehovForBehandling = null,
                        harBehovForTiltak = null,
                        harMulighetForÅKommeIArbeid = null
                    ),
                    paragraf_11_12 = FrontendParagraf_11_12(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417307"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                        bestemmesAv = null,
                        unntak = null,
                        unntaksbegrunnelse = null,
                        manueltSattVirkningsdato = null
                    ),
                    paragraf_11_19 = FrontendParagraf_11_19(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417308"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                        beregningsdato = null,
                    ),
                    paragraf_11_29 = FrontendParagraf_11_29(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417309"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                    ),
                    beregningsdato = FrontendBeregningsdato(null, Autorisasjon.ENDRE),
                    vedtak = null
                )
            )
        )

        assertEquals(expected, saker)
        app.stop()
    }

    @Test
    fun `Lytter på og lagrer mottaker fra utbetaling`() {
        val app = TestApplication {
            environment { config = mocks.applicationConfig() }
            application {
                server(mocks.kafka)
                mottakerTopic = mocks.kafka.inputTopic(Topics.mottakere)
            }
        }

        val client = app.createClient {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                }
            }
        }

        runBlocking { client.get("/actuator/live") }

        assertEquals(0, rowCount(mocks, "mottaker"))

        mottakerTopic.produce("12345678910") {
            DtoMottaker(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                vedtakshistorikk = listOf(
                    DtoVedtak(
                        vedtaksid = UUID.randomUUID(),
                        innvilget = true,
                        grunnlagsfaktor = 4.0,
                        vedtaksdato = LocalDate.of(2022, 5, 2),
                        virkningsdato = LocalDate.of(2022, 5, 2),
                        fødselsdato = LocalDate.of(1990, 1, 1)
                    )
                ),
                aktivitetstidslinje = listOf(
                    DtoMeldeperiode(
                        dager = listOf(
                            DtoDag(
                                dato = LocalDate.of(2022, 5, 2),
                                arbeidstimer = 0.0,
                                type = "ARBEIDSDAG"
                            )
                        )
                    )
                ),
                utbetalingstidslinjehistorikk = listOf(
                    DtoUtbetalingstidslinje(
                        dager = listOf(
                            DtoUtbetalingstidslinjedag(
                                dato = LocalDate.of(2022, 5, 2),
                                grunnlagsfaktor = 4.0,
                                barnetillegg = 0.0,
                                grunnlag = 425596.0,
                                dagsats = 1080.0,
                                høyestebeløpMedBarnetillegg = 1473.0,
                                beløpMedBarnetillegg = 1080.0,
                                beløp = 1080.0,
                                arbeidsprosent = 0.0,
                            )
                        )
                    )
                ),
                oppdragshistorikk = listOf(
                    DtoOppdrag(
                        mottaker = "12345678910",
                        fagområde = "Arbeidsavklaringspenger",
                        linjer = listOf(
                            DtoUtbetalingslinje(
                                fom = LocalDate.of(2022, 5, 2),
                                tom = LocalDate.of(2022, 5, 2),
                                satstype = "DAG",
                                beløp = 1080,
                                aktuellDagsinntekt = 1080,
                                grad = 100,
                                refFagsystemId = null,
                                delytelseId = 1,
                                refDelytelseId = null,
                                endringskode = "NY",
                                klassekode = "RefusjonIkkeOpplysningspliktig",
                                datoStatusFom = null
                            )
                        ),
                        fagsystemId = "NQGM2S4XEJEJ3AYTU7NJDMDNO4",
                        endringskode = "NY",
                        nettoBeløp = 1080,
                        overføringstidspunkt = LocalDate.of(2022, 5, 2).atStartOfDay(),
                        avstemmingsnøkkel = null,
                        status = null,
                        tidsstempel = LocalDate.of(2022, 5, 2).atStartOfDay()
                    )
                ),
                tilstand = "UTBETALING_BEREGNET"
            )
        }

        assertEquals(1, rowCount(mocks, "mottaker"))

        app.stop()
    }

    @Test
    fun `Slett søker ved tombstone`() {
        lateinit var personopplysningerTopic: TestInputTopic<String, PersonopplysningerKafkaDto>

        val app = TestApplication {
            environment { config = mocks.applicationConfig() }
            application {
                server(mocks.kafka)
                søkerTopic = mocks.kafka.inputTopic(Topic("aap.sokere.v1", JsonSerde.jackson()))
                personopplysningerTopic = mocks.kafka.inputTopic(Topics.personopplysninger)
            }
        }

        val client = app.createClient {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                }
            }
        }

        runBlocking { client.get("/actuator/live") }

        søkerTopic.produce("12345678910") {
            SøkereKafkaDto(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                saker = listOf(
                    SøkereKafkaDto.Sak(
                        UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        sakstyper = listOf(
                            SøkereKafkaDto.Sakstype(
                                "STANDARD",
                                true,
                                listOf(
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = UUID.randomUUID().toString(),
                                        vurdertAv = null,
                                        godkjentAv = null,
                                        paragraf = "PARAGRAF_11_2",
                                        ledd = listOf("LEDD_1", "LEDD_2"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = Utfall.IKKE_VURDERT
                                    )
                                )
                            )
                        ),
                        vurderingsdato = LocalDate.of(2022, 1, 1),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        tilstand = "SØKNAD_MOTTATT",
                        vedtak = null
                    )
                ),
                version = SøkereKafkaDto.VERSION
            )
        }

        personopplysningerTopic.produce("12345678910") {
            PersonopplysningerKafkaDto(
                norgEnhetId = "0001",
                adressebeskyttelse = "UGRADERT",
                geografiskTilknytning = "1234",
                skjerming = SkjermingKafkaDto(
                    erSkjermet = false,
                    fom = null,
                    tom = null
                )
            )
        }

        assertEquals(1, client.getSaker("/api/sak", JwtGenerator::generateSaksbehandlerToken).size)

        søkerTopic.produceTombstone("12345678910")
        assertEquals(0, client.getSaker("/api/sak", JwtGenerator::generateSaksbehandlerToken).size)

        assertEquals(0, rowCount(mocks, "soker"))
        assertEquals(0, rowCount(mocks, "personopplysninger"))
        assertEquals(0, rowCount(mocks, "mottaker"))
        app.stop()
    }

    private fun rowCount(mocks: MockEnvironment, tabell: String): Int {
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
        vurdertAv: String?,
        godkjentAv: String?,
        paragraf: String,
        ledd: List<String>,
        tilstand: String,
        utfall: Utfall,
        løsningYrkesskade_manuell: SøkereKafkaDto.LøsningManuellMedlemskapYrkesskade? = null,
        løsningYrkesskade_maskinell: SøkereKafkaDto.LøsningMaskinellMedlemskapYrkesskade? = null,
        løsning_11_2_manuell: SøkereKafkaDto.LøsningManuellParagraf_11_2? = null,
        løsning_11_2_maskinell: SøkereKafkaDto.LøsningMaskinellParagraf_11_2? = null,
        løsning_11_3_manuell: SøkereKafkaDto.LøsningParagraf_11_3? = null,
        løsning_11_4_l2_l3_manuell: SøkereKafkaDto.LøsningParagraf_11_4_ledd2_ledd3? = null,
        løsning_11_5_manuell: SøkereKafkaDto.LøsningParagraf_11_5? = null,
        løsning_11_5_yrkesskade_manuell: SøkereKafkaDto.LøsningParagraf_11_5_yrkesskade? = null,
        løsning_11_6_manuell: SøkereKafkaDto.LøsningParagraf_11_6? = null,
        løsning_11_12_l1_manuell: SøkereKafkaDto.LøsningParagraf_11_12_ledd1? = null,
        løsning_11_22_manuell: SøkereKafkaDto.LøsningParagraf_11_22? = null,
        løsning_11_29_manuell: SøkereKafkaDto.LøsningParagraf_11_29? = null
    ) = SøkereKafkaDto.Vilkårsvurdering(
        vilkårsvurderingsid = UUID.fromString(vilkårsvurderingsid),
        vurdertAv = vurdertAv,
        godkjentAv = godkjentAv,
        paragraf = paragraf,
        ledd = ledd,
        tilstand = tilstand,
        utfall = utfall,
        løsning_medlemskap_yrkesskade_maskinell = løsningYrkesskade_maskinell?.let(::listOf),
        løsning_medlemskap_yrkesskade_manuell = løsningYrkesskade_manuell?.let(::listOf),
        løsning_11_2_maskinell = løsning_11_2_maskinell?.let(::listOf),
        løsning_11_2_manuell = løsning_11_2_manuell?.let(::listOf),
        løsning_11_3_manuell = løsning_11_3_manuell?.let(::listOf),
        løsning_11_4_ledd2_ledd3_manuell = løsning_11_4_l2_l3_manuell?.let(::listOf),
        løsning_11_5_manuell = løsning_11_5_manuell?.let(::listOf),
        løsning_11_5_yrkesskade_manuell = løsning_11_5_yrkesskade_manuell?.let(::listOf),
        løsning_11_6_manuell = løsning_11_6_manuell?.let(::listOf),
        løsning_11_12_ledd1_manuell = løsning_11_12_l1_manuell?.let(::listOf),
        løsning_11_22_manuell = løsning_11_22_manuell?.let(::listOf),
        løsning_11_29_manuell = løsning_11_29_manuell?.let(::listOf),
    )

    private fun HttpClient.getSaker(path: String, tokenSupplier: () -> SignedJWT): List<FrontendSøker> = runBlocking {
        val response = get(path) {
            bearerAuth(tokenSupplier().serialize())
            accept(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        response.body()
    }
}
