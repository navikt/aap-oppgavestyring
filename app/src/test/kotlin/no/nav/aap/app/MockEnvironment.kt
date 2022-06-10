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
        "AAP_SAKSBEHANDLER" to "9eea5eb0-1f42-4661-949a-91740d817f49",
        "AAP_BESLUTTER" to "bcc57777-aba4-45ef-8f07-fa594e54a33f",
        "AAP_VEILEDER" to "33b4d871-e3de-472e-be8a-762cb25c23d8",
        "AAP_FATTER" to "8f9c8d32-a4b5-4baf-95c4-3710f48edfe7",
        "AAP_LES" to "05eab1c7-9877-4566-95ee-87ab960a3c42",
        "FORTROLIG_ADRESSE" to "5a749147-f49a-494f-94f4-19e38c3031cf",
        "STRENGT_FORTROLIG_ADRESSE" to "8bc149cb-d73e-44af-8a89-07ccef99c22c",
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
