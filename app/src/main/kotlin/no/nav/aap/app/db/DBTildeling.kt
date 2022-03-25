package no.nav.aap.app.db

import java.util.UUID

data class DBTildeling(
    val saksid: UUID,
    val ident: String,
    val rolle: String
)