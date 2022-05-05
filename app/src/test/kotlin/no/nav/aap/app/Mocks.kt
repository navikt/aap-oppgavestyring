package no.nav.aap.app

import com.nimbusds.jwt.SignedJWT
import no.nav.aap.kafka.streams.test.KafkaStreamsMock
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.apache.kafka.streams.TestInputTopic
import org.testcontainers.containers.PostgreSQLContainer

class Mocks : AutoCloseable {
    val azure = AzureMock().apply { start() }
    val kafka = KafkaStreamsMock()
    val postgres = PostgreSQLContainer<Nothing>("postgres:14").apply { start() }

    override fun close() {
        azure.close()
        postgres.close()
    }
}

class AzureMock(private val server: MockOAuth2Server = MockOAuth2Server()) {
    fun wellKnownUrl(): String = server.wellKnownUrl("azure").toString()
    fun issueAzureToken(): SignedJWT = server.issueToken(issuerId = "azure", audience = "oppgavestyring")
    fun start() = server.start()
    fun close() = server.shutdown()
}

inline fun <reified V : Any> TestInputTopic<String, V>.produce(key: String, value: () -> V) = pipeInput(key, value())
inline fun <reified V : Any> TestInputTopic<String, V>.produceTombstone(key: String) = pipeInput(key, null)
