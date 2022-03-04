package no.nav.aap.app

import com.nimbusds.jwt.SignedJWT
import no.nav.aap.app.kafka.Kafka
import no.nav.aap.app.kafka.KafkaConfig
import no.nav.aap.app.kafka.Topic
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class Mocks : AutoCloseable {
    val azure = AzureMock().apply { start() }
    val kafka = KafkaSetupMock()

    override fun close() {
        azure.close()
    }
}

class AzureMock(private val server: MockOAuth2Server = MockOAuth2Server()) {
    fun wellKnownUrl(): String = server.wellKnownUrl("azure").toString()
    fun issueAzureToken(): SignedJWT = server.issueToken(issuerId = "azure", audience = "oppgavestyring")
    fun start() = server.start()
    fun close() = server.shutdown()
}

class KafkaSetupMock : Kafka {
    lateinit var config: KafkaConfig

    override fun start(kafkaConfig: KafkaConfig) {
        config = kafkaConfig
    }

    internal val schemaRegistryUrl: String by lazy { "mock://schema-registry/${UUID.randomUUID()}" }

    override fun <V : Any> createProducer(topic: Topic<String, V>): Producer<String, V> =
        object : MockProducer<String, V>(true, topic.keySerde.serializer(), topic.valueSerde.serializer()) {
            override fun send(record: ProducerRecord<String, V>): Future<RecordMetadata> {
                return object : Future<RecordMetadata> {
                    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                        TODO("Not yet implemented")
                    }

                    override fun isCancelled(): Boolean {
                        TODO("Not yet implemented")
                    }

                    override fun isDone(): Boolean {
                        TODO("Not yet implemented")
                    }

                    override fun get() =
                        RecordMetadata(TopicPartition(topic.name, 0), 0, 0, 0, 0, 0)

                    override fun get(timeout: Long, unit: TimeUnit): RecordMetadata {
                        TODO("Not yet implemented")
                    }
                }
            }
        }
}

