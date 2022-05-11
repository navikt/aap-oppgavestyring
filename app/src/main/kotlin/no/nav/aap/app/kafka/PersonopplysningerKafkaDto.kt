package no.nav.aap.app.kafka

import no.nav.aap.app.frontendView.FrontendPersonopplysninger
import java.time.LocalDate

data class PersonopplysningerKafkaDto(
    val norgEnhetId: String? = null,
    val adressebeskyttelse: String? = null,
    val geografiskTilknytning: String? = null,
    val skjerming: SkjermingKafkaDto? = null,
) {
    fun toFrontendView(personident: String) = FrontendPersonopplysninger(
        personident = personident,
        norgEnhetId = norgEnhetId!!,
        adressebeskyttelse = adressebeskyttelse!!,
        geografiskTilknytning = geografiskTilknytning!!,
        erSkjermet = skjerming!!.erSkjermet,
        erSkjermetFom = skjerming.fom,
        erSkjermetTom = skjerming.tom,
    )
}

data class SkjermingKafkaDto(
    val erSkjermet: Boolean,
    val fom: LocalDate?,
    val tom: LocalDate?
)
