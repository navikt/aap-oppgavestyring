package no.nav.aap.app.modell

internal class Skjermet(private val erSkjermet: Boolean) {
    internal fun toDBString() = erSkjermet
}