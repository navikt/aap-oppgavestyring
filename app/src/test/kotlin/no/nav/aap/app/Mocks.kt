package no.nav.aap.app

import com.nimbusds.jwt.SignedJWT
import io.confluent.kafka.schemaregistry.testutil.MockSchemaRegistry
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import no.nav.aap.app.kafka.Kafka
import no.nav.aap.app.kafka.KafkaConfig
import no.nav.aap.app.kafka.Topic
import no.nav.aap.app.kafka.plus
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.state.KeyValueStore
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class Mocks : AutoCloseable {
    val azure = AzureMock().apply { start() }
    val kafka = KafkaSetupMock()
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

class KafkaSetupMock : Kafka {
    lateinit var driver: TopologyTestDriver
    lateinit var config: KafkaConfig

    override fun start(topology: Topology, kafkaConfig: KafkaConfig) {
        driver = TopologyTestDriver(topology, kafkaConfig.consumer + kafkaConfig.producer + testConfig)
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

    override fun <V : Any> createConsumer(topic: Topic<String, V>): Consumer<String, V> =
        MockConsumer(OffsetResetStrategy.EARLIEST)
    override fun <V> getStore(name: String): ReadOnlyKeyValueStore<String, V> = driver.getKeyValueStore(name)
    override fun close() = driver.close().also { MockSchemaRegistry.dropScope(schemaRegistryUrl) }
    override fun healthy(): Boolean = true
    override fun started(): Boolean = true

    fun <V : SpecificRecord> inputAvroTopic(name: String): TestInputTopic<String, V> {
        val serde = SpecificAvroSerde<V>().apply { configure(avroConfig, false) }
        return driver.createInputTopic(name, Serdes.String().serializer(), serde.serializer())
    }

    fun <V : SpecificRecord> outputAvroTopic(name: String): TestOutputTopic<String, V> {
        val serde = SpecificAvroSerde<V>().apply { configure(avroConfig, false) }
        return driver.createOutputTopic(name, Serdes.String().deserializer(), serde.deserializer())
    }

    inline fun <reified V : Any> getKeyValueStore(storeName: String): KeyValueStore<String, V> =
        driver.getKeyValueStore(storeName)

    private val testConfig = Properties().apply {
        this[StreamsConfig.STATE_DIR_CONFIG] = "build/kafka-streams/state"
        this[StreamsConfig.MAX_TASK_IDLE_MS_CONFIG] = StreamsConfig.MAX_TASK_IDLE_MS_DISABLED
    }

    private val avroConfig: Map<String, String>
        get() = mapOf(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG to config.schemaRegistryUrl)
}

inline fun <reified V : Any> TestInputTopic<String, V>.produce(key: String, value: () -> V) = pipeInput(key, value())
inline fun <reified V : Any> TestInputTopic<String, V>.produceTombstone(key: String) = pipeInput(key, null)
