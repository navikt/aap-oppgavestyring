package no.nav.aap.app

import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.aap.kafka.streams.test.KafkaStreamsMock
import org.apache.kafka.streams.TestInputTopic
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy
import java.time.Duration

class MockEnvironment : AutoCloseable {
    val postgres: PostgreSQLContainer<Nothing> = PostgreSQLContainer<Nothing>("postgres:14").apply {
        withStartupCheckStrategy(MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(2)))
        start()
    }

    private val oauth = embeddedServer(Netty, port = 9999) {
        install(ContentNegotiation) { jackson {} }
        routing {
            get("/jwks") {
                call.respond(this::class.java.getResource("/jwkset.json")!!.readText())
            }
        }
    }.start()

    val kafka: KafkaStreamsMock = KafkaStreamsMock()

    override fun close() {
        postgres.close()
        oauth.stop()
    }

    fun applicationConfig() = MapApplicationConfig(
        "AZURE_OPENID_CONFIG_ISSUER" to "azure",
        "AZURE_APP_CLIENT_ID" to "oppgavestyring",
        "AZURE_OPENID_CONFIG_JWKS_URI" to "http://localhost:9999/jwks",
        "KAFKA_STREAMS_APPLICATION_ID" to "oppgavestyring",
        "KAFKA_BROKERS" to "mock://kafka",
        "KAFKA_SECURITY_ENABLED" to "false",
        "KAFKA_TRUSTSTORE_PATH" to "",
        "KAFKA_KEYSTORE_PATH" to "",
        "KAFKA_CREDSTORE_PASSWORD" to "",
        "KAFKA_CLIENT_ID" to "oppgavestyring",
        "DB_HOST" to postgres.host,
        "DB_PORT" to postgres.firstMappedPort.toString(),
        "DB_DATABASE" to postgres.databaseName,
        "DB_USERNAME" to postgres.username,
        "DB_PASSWORD" to postgres.password
    )
}

inline fun <reified V : Any> TestInputTopic<String, V>.produce(key: String, value: () -> V) = pipeInput(key, value())
inline fun <reified V : Any> TestInputTopic<String, V>.produceTombstone(key: String) = pipeInput(key, null)
