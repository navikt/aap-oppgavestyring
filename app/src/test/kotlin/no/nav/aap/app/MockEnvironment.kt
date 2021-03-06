package no.nav.aap.app

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.app.axsys.Enhet
import no.nav.aap.app.axsys.Tilganger
import no.nav.aap.app.security.TestAzureGroups
import no.nav.aap.kafka.streams.test.KafkaStreamsMock
import org.apache.kafka.streams.TestInputTopic
import org.testcontainers.containers.PostgreSQLContainer

class MockEnvironment : AutoCloseable {
    val postgres = PostgreSQLContainer<Nothing>("postgres:14").apply { start() }

    private val azureAd = embeddedServer(Netty, port = 0, module = Application::azureAdMock).apply { start() }

    private val oauth = embeddedServer(Netty, port = 9999) {
        install(ContentNegotiation) { jackson {} }
        routing {
            get("/jwks") {
                call.respondText(this::class.java.getResource("/jwkset.json")!!.readText())
            }
        }
    }.start()

    private val axsys = embeddedServer(Netty, port = 0) {
        install(ContentNegotiation) { jackson {} }
        routing {
            get("/tilgang/Z000001") {
                call.respond(
                    HttpStatusCode.OK, Tilganger(
                        enheter = listOf(
                            Enhet(
                                enhetId = "0001",
                                navn = "Testenhet",
                                temaer = listOf()
                            )
                        )
                    )
                )
            }
        }
    }.start()

    val kafka: KafkaStreamsMock = KafkaStreamsMock()

    override fun close() {
        postgres.close()
        oauth.stop()
        axsys.stop()
        azureAd.stop()
    }

    fun applicationConfig() = MapApplicationConfig(
        "AZURE_OPENID_CONFIG_ISSUER" to "azure",
        "AZURE_APP_CLIENT_ID" to "oppgavestyring",
        "AZURE_OPENID_CONFIG_JWKS_URI" to "http://localhost:9999/jwks",
        "AZURE_OPENID_CONFIG_TOKEN_ENDPOINT" to "http://localhost:${azureAd.port}/token",
        "AZURE_APP_CLIENT_SECRET" to "test",
        "KAFKA_STREAMS_APPLICATION_ID" to "oppgavestyring",
        "KAFKA_BROKERS" to "mock://kafka",
        "KAFKA_TRUSTSTORE_PATH" to "",
        "KAFKA_KEYSTORE_PATH" to "",
        "KAFKA_CREDSTORE_PASSWORD" to "",
        "AAP_SAKSBEHANDLER" to TestAzureGroups.SAKSBEHANDLER.uuid,
        "AAP_BESLUTTER" to TestAzureGroups.BESLUTTER.uuid,
        "AAP_VEILEDER" to TestAzureGroups.VEILEDER.uuid,
        "AAP_FATTER" to TestAzureGroups.FATTER.uuid,
        "AAP_LES" to TestAzureGroups.LES.uuid,
        "FORTROLIG_ADRESSE" to TestAzureGroups.FORTROLIG_ADRESSE.uuid,
        "STRENGT_FORTROLIG_ADRESSE" to TestAzureGroups.STRENGT_FORTROLIG_ADRESSE.uuid,
        "DB_HOST" to postgres.host,
        "DB_PORT" to postgres.firstMappedPort.toString(),
        "DB_DATABASE" to postgres.databaseName,
        "DB_USERNAME" to postgres.username,
        "DB_PASSWORD" to postgres.password,
        "AXSYS_SCOPE" to "test",
        "AXSYS_URL" to "http://localhost:${axsys.port}"
    )

    companion object {
        val NettyApplicationEngine.port get() = runBlocking { resolvedConnectors() }.first { it.type == ConnectorType.HTTP }.port
    }
}

inline fun <reified V : Any> TestInputTopic<String, V>.produce(key: String, value: () -> V) = pipeInput(key, value())
inline fun <reified V : Any> TestInputTopic<String, V>.produceTombstone(key: String) = pipeInput(key, null)
