package no.nav.aap.app.modell

internal enum class Diskresjonskode {
    UGRADERT,
    FORTROLIG,
    STRENGT_FORTROLIG,
    STRENGT_FORTROLIG_UTLAND;

    internal fun toDBString() = name
}