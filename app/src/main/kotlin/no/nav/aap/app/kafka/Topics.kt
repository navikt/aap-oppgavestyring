package no.nav.aap.app.kafka

import no.nav.aap.kafka.serde.json.JsonSerde
import no.nav.aap.kafka.streams.Topic

object Topics {
    val søkere = Topic("aap.sokere.v1", JsonSerde.jackson<SøkereKafkaDto>())
    val manuell = Topic("aap.manuell.v1", JsonSerde.jackson<ManuellKafkaDto>())
    val personopplysninger = Topic("aap.personopplysninger.v1", JsonSerde.jackson<PersonopplysningerKafkaDto>())
    val mottakere = Topic("aap.mottakere.v1", JsonSerde.jackson<DtoMottaker>())
}
