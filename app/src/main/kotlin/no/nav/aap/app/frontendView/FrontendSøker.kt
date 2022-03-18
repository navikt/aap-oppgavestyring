package no.nav.aap.app.frontendView

data class FrontendSøker(
    val personident: String,
    val saker: List<FrontendSak>
)
