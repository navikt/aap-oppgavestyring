package no.nav.aap.app.kafka

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import no.nav.aap.avro.manuell.v1.Manuell as AvroManuell

data class Topic<K, V>(
    val name: String,
    val keySerde: Serde<K>,
    val valueSerde: Serde<V>,
)

class Topics(private val config: KafkaConfig) {
    val manuell = Topic("aap.manuell.v1", Serdes.StringSerde(), avroSerde<AvroManuell>())

    private fun <T : SpecificRecord> avroSerde(): SpecificAvroSerde<T> = SpecificAvroSerde<T>().apply {
        val avroProperties = config.schemaRegistry + config.ssl
        val avroConfig = avroProperties.map { it.key.toString() to it.value.toString() }
        configure(avroConfig.toMap(), false)
    }
}
