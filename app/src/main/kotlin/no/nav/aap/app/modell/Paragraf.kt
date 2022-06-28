package no.nav.aap.app.modell

internal enum class Paragraf(internal val skalLøsesAv: Enhet) {
    PARAGRAF_11_2(Enhet.NAY),
    PARAGRAF_11_3(Enhet.NAY),
    PARAGRAF_11_4(Enhet.NAY),
    PARAGRAF_11_5(Enhet.KONTOR),
    PARAGRAF_11_6(Enhet.NAY),
    PARAGRAF_11_9(Enhet.KONTOR),
    PARAGRAF_11_10(Enhet.KONTOR),
    PARAGRAF_11_11(Enhet.KONTOR),
    PARAGRAF_11_12(Enhet.NAY),
    PARAGRAF_11_14(Enhet.NAY),
    PARAGRAF_11_19(Enhet.NAY),
    PARAGRAF_11_29(Enhet.NAY)
}

internal enum class Ledd {
    LEDD_1,
    LEDD_2,
    LEDD_3;

    operator fun plus(other: Ledd) = listOf(this, other)
}