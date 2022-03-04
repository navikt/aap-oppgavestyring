package no.nav.aap.app.kafka

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer

interface Kafka {
    fun start(kafkaConfig: KafkaConfig)
    fun <V : Any> createProducer(topic: Topic<String, V>): Producer<String, V>
}

class KafkaSetup : Kafka {
    private lateinit var config: KafkaConfig

    override fun start(kafkaConfig: KafkaConfig) {
        config = kafkaConfig
    }

    override fun <V : Any> createProducer(topic: Topic<String, V>): Producer<String, V> =
        KafkaProducer(
            config.producer + mapOf(CommonClientConfigs.CLIENT_ID_CONFIG to "client-${topic.name}"),
            topic.keySerde.serializer(),
            topic.valueSerde.serializer()
        )
}
