package no.nav.aap.app.frontendView

import java.time.LocalDate

data class FrontendSøker(
    val personident: String,
    val fødselsdato: LocalDate,
    val sak: FrontendSak,
    val skjermet: Boolean
)
