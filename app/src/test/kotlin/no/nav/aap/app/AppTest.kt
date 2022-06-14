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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.frontendView.*
import no.nav.aap.app.kafka.*
import no.nav.aap.app.kafka.DtoVedtak
import no.nav.aap.app.security.JwtGenerator
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
    private lateinit var manuellOutputTopic: TestOutputTopic<String, ManuellKafkaDto>

    @BeforeAll
    fun setupMockEnvironment() {
        mocks = MockEnvironment()
    }

    @AfterAll
    fun closeMockEnvironment() = mocks.close()

    @AfterEach
    fun assertNoUnassertedRecordsLeftOnTopic() {
        if (::manuellOutputTopic.isInitialized) {
            manuellOutputTopic.readAndAssert().isEmpty()
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
    fun `Authentisering av endepunkt for sending av løsning`() {
        testApplication {
            environment { config = mocks.applicationConfig() }
            application { server(mocks.kafka) }

            postLøsning("""{"løsning_11_3_manuell":{"erOppfylt":true}}""")
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
            client.post("/api/sak/123/losning") {
                bearerAuth(JwtGenerator.generateSaksbehandlerToken().serialize())
                contentType(ContentType.Application.Json)
                setBody(DtoManuell(løsning_11_3_manuell = DtoLøsningParagraf_11_3(
                    erOppfylt = true
                )))
            }
        }

        val producer = mocks.kafka.getProducer(Topics.manuell)
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
                søkerTopic = mocks.kafka.inputTopic(Topics.søkere)
                personopplysningerTopic = mocks.kafka.inputTopic(Topics.personopplysninger)
                mocks.kafka.outputTopic(Topics.manuell)
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
                                        utfall = "IKKE_VURDERT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417302",
                                        paragraf = "PARAGRAF_11_3",
                                        ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = "IKKE_VURDERT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417303",
                                        paragraf = "PARAGRAF_11_4",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "OPPFYLT",
                                        utfall = "OPPFYLT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417304",
                                        paragraf = "PARAGRAF_11_4",
                                        ledd = listOf("LEDD_2", "LEDD_3"),
                                        tilstand = "IKKE_RELEVANT",
                                        utfall = "IKKE_RELEVANT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417305",
                                        paragraf = "PARAGRAF_11_5",
                                        ledd = listOf("LEDD_1", "LEDD_2"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = "IKKE_VURDERT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417306",
                                        paragraf = "PARAGRAF_11_6",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = "IKKE_VURDERT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417307",
                                        paragraf = "PARAGRAF_11_12",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = "IKKE_VURDERT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417308",
                                        paragraf = "PARAGRAF_11_29",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = "IKKE_VURDERT"
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
                    paragraf_11_29 = FrontendParagraf_11_29(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417308"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                    ),
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
                søkerTopic = mocks.kafka.inputTopic(Topics.søkere)
                personopplysningerTopic = mocks.kafka.inputTopic(Topics.personopplysninger)
                mocks.kafka.outputTopic(Topics.manuell)
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
                                        utfall = "IKKE_VURDERT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417302",
                                        paragraf = "PARAGRAF_11_3",
                                        ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = "IKKE_VURDERT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417303",
                                        paragraf = "PARAGRAF_11_4",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "OPPFYLT",
                                        utfall = "OPPFYLT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417304",
                                        paragraf = "PARAGRAF_11_4",
                                        ledd = listOf("LEDD_2", "LEDD_3"),
                                        tilstand = "IKKE_RELEVANT",
                                        utfall = "IKKE_RELEVANT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417305",
                                        paragraf = "PARAGRAF_11_5",
                                        ledd = listOf("LEDD_1", "LEDD_2"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = "IKKE_VURDERT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417306",
                                        paragraf = "PARAGRAF_11_6",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = "IKKE_VURDERT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417307",
                                        paragraf = "PARAGRAF_11_12",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = "IKKE_VURDERT"
                                    ),
                                    vilkarsvurdering(
                                        vilkårsvurderingsid = "f422222c-8606-4426-b929-c2b8b4417308",
                                        paragraf = "PARAGRAF_11_29",
                                        ledd = listOf("LEDD_1"),
                                        tilstand = "SØKNAD_MOTTATT",
                                        utfall = "IKKE_VURDERT"
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
                    paragraf_11_29 = FrontendParagraf_11_29(
                        vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417308"),
                        utfall = "IKKE_VURDERT",
                        autorisasjon = Autorisasjon.ENDRE,
                    ),
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
                mocks.kafka.outputTopic(Topics.manuell)
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
                søkerTopic = mocks.kafka.inputTopic(Topics.søkere)
                personopplysningerTopic = mocks.kafka.inputTopic(Topics.personopplysninger)
                mocks.kafka.outputTopic(Topics.manuell)
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
                                        utfall = "IKKE_VURDERT"
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
        assertEquals(0, rowCount(mocks, "sak"))
        assertEquals(0, rowCount(mocks, "oppgave"))
        assertEquals(0, rowCount(mocks, "rolle"))
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
        paragraf: String,
        ledd: List<String>,
        tilstand: String,
        utfall: String,
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
        utfall = utfall,
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

    private suspend fun ApplicationTestBuilder.postLøsning(body: String) {
        val response = client.post("/api/sak/123/losning") {
            bearerAuth(JwtGenerator.generateSaksbehandlerToken().serialize())
            contentType(ContentType.Application.Json)
            setBody(body)
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    private fun HttpClient.getSaker(path: String, tokenSupplier: () -> SignedJWT): List<FrontendSøker> = runBlocking {
        val response = get(path) {
            bearerAuth(tokenSupplier().serialize())
            accept(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        response.body()
    }
}
