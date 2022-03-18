package no.nav.aap.app.modell

internal class Personident(private val ident: String) {
    internal fun toDBString() = ident
}