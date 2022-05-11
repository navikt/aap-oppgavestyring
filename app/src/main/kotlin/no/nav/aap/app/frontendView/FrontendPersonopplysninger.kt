package no.nav.aap.app.frontendView

import java.time.LocalDate

data class FrontendPersonopplysninger(
    val personident: String,
    val norgEnhetId: String,
    val adressebeskyttelse: String,
    val geografiskTilknytning: String,
    val erSkjermet: Boolean,
    val erSkjermetFom: LocalDate?,
    val erSkjermetTom: LocalDate?
)
