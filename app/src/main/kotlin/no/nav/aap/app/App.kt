package no.nav.aap.app

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.aap.app.axsys.AxsysClient
import no.nav.aap.app.db.DbConfig
import no.nav.aap.app.frontendView.toFrontendView
import no.nav.aap.app.kafka.PersonopplysningerKafkaDto
import no.nav.aap.app.kafka.Topics
import no.nav.aap.kafka.KafkaConfig
import no.nav.aap.kafka.streams.*
import no.nav.aap.ktor.config.loadConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.streams.StreamsBuilder
import org.flywaydb.core.Flyway
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.sql.DataSource

private val secureLog = LoggerFactory.getLogger("secureLog")

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::server).start(wait = true)
}

internal fun Application.server(kafka: KStreams = KafkaStreams) {
    val prometheus = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val config = loadConfig<Config>()

    install(MicrometerMetrics) { registry = prometheus }
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    val jwkProvider: JwkProvider = JwkProviderBuilder(config.oauth.azure.jwksUrl)
        .cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES)
        .build()

    install(Authentication) {
        fun AuthenticationConfig.jwt(name: String, realm: String, roles: List<RoleName>? = null) = jwt(name) {
            this.realm = realm
            verifier(jwkProvider, config.oauth.azure.issuer)
            challenge { _, _ -> call.respond(HttpStatusCode.Unauthorized, "oppgavestyring sin dørvakt stoppet deg") }
            validate { cred ->
                if (cred.getClaim("preferred_username", String::class) == null) return@validate null
                if (cred.getClaim("NAVident", String::class) == null) return@validate null

                val claimedRoles = cred.getListClaim("groups", UUID::class)
                val authorizedRoles = config.oauth.roles
                    .filter { roles?.contains(it.name) ?: true }
                    .map { it.objectId }
                if (claimedRoles.none(authorizedRoles::contains)) return@validate null

                JWTPrincipal(cred.payload)
            }
        }
        jwt("hentStuff", "hent oppgaver")
        jwt("løsningNAY", "løsning NAY", listOf(RoleName.SAKSBEHANDLER, RoleName.BESLUTTER))
        jwt("løsningLokalkontor", "løsning lokalkontor", listOf(RoleName.VEILEDER, RoleName.FATTER))
    }

    environment.monitor.subscribe(ApplicationStopping) { kafka.close() }

    val datasource = initDatasource(config.database)
    migrate(datasource)
    val repo = Repo(datasource)
    val innloggetBrukerProvider = InnloggetBrukerProvider(AxsysClient(config.axsys, config.azure), config.oauth.roles)

    kafka.connect(config.kafka, prometheus, topology(repo))

    routing {
        actuator(prometheus, kafka)
        api(innloggetBrukerProvider, repo, config.kafka, kafka)
    }
}

internal fun topology(repo: Repository) = StreamsBuilder().apply {
    val søkerKStream = consume(Topics.søkere)

    consume(Topics.personopplysninger)
        .filterNotNull("filter-personopplysning-tombstones")
        .filterNot(erUfullstendig)
        .peek { key, value -> secureLog.info("saving [${Topics.personopplysninger}] K:$key V:$value") }
        .foreach { personident, value -> repo.lagrePersonopplysninger(value.toFrontendView(personident)) }

    val presentSøkereStream = søkerKStream.filterNotNull("filter-sokere-tombstone")

    presentSøkereStream
        .peek { key, value -> secureLog.info("saving [${Topics.søkere}] K:$key V:$value") }
        .foreach { _, value -> repo.lagreSøker(value) }

    presentSøkereStream
        .mapValues { _, _ -> PersonopplysningerKafkaDto() }
        .produce(Topics.personopplysninger, "produce-empty-personopplysninger")

    søkerKStream.filter { _, value -> value == null }
        .peek { key, value -> secureLog.info("deleted [${Topics.søkere}] K:$key V:$value") }
        .foreach { key, _ -> repo.slettSøker(key) }

    consume(Topics.mottakere)
        .filterNotNull("filter-mottakere-tombstone")
        .foreach { _, mottaker -> repo.lagreMottaker(mottaker.toFrontendView()) }
}.build()

private val erUfullstendig: (_: String, value: PersonopplysningerKafkaDto) -> Boolean = { k, v ->
    listOf(v.norgEnhetId, v.adressebeskyttelse, v.geografiskTilknytning, v.skjerming)
        .any { it == null }
        .also { if (it) secureLog.info("skipped [${Topics.personopplysninger}] K:$k V:$v") }
}

private fun Routing.actuator(prometheus: PrometheusMeterRegistry, kafka: KStreams) {
    route("/actuator") {
        get("/metrics") {
            call.respond(prometheus.scrape())
        }

        get("/live") {
            val status = if (kafka.isLive()) HttpStatusCode.OK else HttpStatusCode.InternalServerError
            call.respond(status, "oppgavestyring")
        }

        get("/ready") {
            val status = if (kafka.isReady()) HttpStatusCode.OK else HttpStatusCode.InternalServerError
            call.respond(status, "oppgavestyring")
        }
    }
}

private fun Routing.api(
    innloggetBrukerProvider: InnloggetBrukerProvider,
    repo: Repo,
    config: KafkaConfig,
    kafka: KStreams
) {
    val manuellProducer = kafka.createProducer(config, Topics.manuell)

    route("/api") {
        authenticate("hentStuff") {
            get("/personopplysninger/{personident}") {
                val personident = call.parameters.getOrFail("personident")
                when (val opplysninger = repo.hentPersonopplysninger(personident)) {
                    null -> call.respond(HttpStatusCode.NotFound)
                    else -> call.respond(HttpStatusCode.OK, opplysninger)
                }
            }

            get("/sak") {
                call.respond(repo.hentSøkere(innloggetBrukerProvider.hentInnloggetBruker(call.principal()!!)))
            }

            get("/sak/{personident}") {
                val personident = call.parameters.getOrFail("personident")
                call.respond(
                    repo.hentSøker(personident, innloggetBrukerProvider.hentInnloggetBruker(call.principal()!!))
                )
            }
        }

        fun Route.postLøsning(path: String, block: suspend ApplicationCall.() -> DtoManuell) = post(path) {
            val personident = call.parameters.getOrFail("personident")
            secureLog.info("Skal løse oppgave for $personident")
            val innloggetBruker = innloggetBrukerProvider.hentInnloggetBruker(call.principal()!!)
            val manuell = call.block()
            withContext(Dispatchers.IO) {
                manuellProducer.send(
                    ProducerRecord(Topics.manuell.name, personident, manuell.toKafkaDto(innloggetBruker.brukernavn))
                ).get()
            }
            call.respond(HttpStatusCode.OK, "OK")
        }

        authenticate("løsningNAY") {
            postLøsning("/sak/{personident}/losning/paragraf_11_2") {
                DtoManuell(løsning_11_2_manuell = receive())
            }
            postLøsning("/sak/{personident}/losning/paragraf_11_3") {
                DtoManuell(løsning_11_3_manuell = receive())
            }
            postLøsning("/sak/{personident}/losning/paragraf_11_4") {
                DtoManuell(løsning_11_4_ledd2_ledd3_manuell = receive())
            }
            postLøsning("/sak/{personident}/losning/paragraf_11_6") {
                DtoManuell(løsning_11_6_manuell = receive())
            }
            postLøsning("/sak/{personident}/losning/paragraf_11_12") {
                DtoManuell(løsning_11_12_ledd1_manuell = receive())
            }
            postLøsning("/sak/{personident}/losning/paragraf_11_29") {
                DtoManuell(løsning_11_29_manuell = receive())
            }
        }

        authenticate("løsningLokalkontor") {
            postLøsning("/sak/{personident}/losning/paragraf_11_5") {
                DtoManuell(løsning_11_5_manuell = receive())
            }
        }
    }
}

private fun initDatasource(dbConfig: DbConfig) = HikariDataSource(HikariConfig().apply {
    jdbcUrl = dbConfig.url
    username = dbConfig.username
    password = dbConfig.password
    maximumPoolSize = 3
    minimumIdle = 1
    initializationFailTimeout = 5000
    idleTimeout = 10001
    connectionTimeout = 1000
    maxLifetime = 30001
})

private fun migrate(dataSource: DataSource) {
    Flyway
        .configure()
        .cleanOnValidationError(true)
        .dataSource(dataSource)
        .load()
        .migrate()
}
