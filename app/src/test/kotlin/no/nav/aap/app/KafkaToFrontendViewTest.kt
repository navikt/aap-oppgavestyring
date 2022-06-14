package no.nav.aap.app

import no.nav.aap.app.axsys.InnloggetBruker
import no.nav.aap.app.frontendView.Autorisasjon
import no.nav.aap.app.frontendView.Utfall
import no.nav.aap.app.frontendView.toFrontendView
import no.nav.aap.app.kafka.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class KafkaToFrontendViewTest {

    @Test
    fun `Innlogget saksbehandler kan ikke redigere paragraf 11-5, men redigere alle andre`() {
        val kafkaDto = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = listOf(
                Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.OPPFYLT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            )
        )

        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.SAKSBEHANDLER),
            tilknyttedeEnheter = listOf("0001")
        )

        val frontendView = kafkaDto.toFrontendView(innloggetBruker)

        val p11_2 = requireNotNull(frontendView.sak.paragraf_11_2) { "Paragraf 11-2 skal ikke være null her" }
        assertEquals(Autorisasjon.ENDRE, p11_2.autorisasjon)

        val p11_3 = requireNotNull(frontendView.sak.paragraf_11_3) { "Paragraf 11-3 skal ikke være null her" }
        assertEquals(Autorisasjon.ENDRE, p11_3.autorisasjon)

        val p11_4 = requireNotNull(frontendView.sak.paragraf_11_4) { "Paragraf 11-4 skal ikke være null her" }
        assertEquals(Autorisasjon.ENDRE, p11_4.autorisasjon)

        val p11_5 = requireNotNull(frontendView.sak.paragraf_11_5) { "Paragraf 11-5 skal ikke være null her" }
        assertEquals(Autorisasjon.LESE, p11_5.autorisasjon)

        val p11_6 = requireNotNull(frontendView.sak.paragraf_11_6) { "Paragraf 11-6 skal ikke være null her" }
        assertEquals(Autorisasjon.ENDRE, p11_6.autorisasjon)

        val p11_12 = requireNotNull(frontendView.sak.paragraf_11_12) { "Paragraf 11-12 skal ikke være null her" }
        assertEquals(Autorisasjon.ENDRE, p11_12.autorisasjon)

        val p11_29 = requireNotNull(frontendView.sak.paragraf_11_29) { "Paragraf 11-29 skal ikke være null her" }
        assertEquals(Autorisasjon.ENDRE, p11_29.autorisasjon)
    }

    @Test
    fun `Innlogget veileder kan redigere paragraf 11-5, men ikke redigere alle andre`() {
        val kafkaDto = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = listOf(
                Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.OPPFYLT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            )
        )

        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@est.com",
            roller = listOf(RoleName.VEILEDER),
            tilknyttedeEnheter = listOf("0001")
        )

        val frontendView = kafkaDto.toFrontendView(innloggetBruker)

        val p11_2 = requireNotNull(frontendView.sak.paragraf_11_2) { "Paragraf 11-2 skal ikke være null her" }
        assertEquals(Autorisasjon.LESE, p11_2.autorisasjon)

        val p11_3 = requireNotNull(frontendView.sak.paragraf_11_3) { "Paragraf 11-3 skal ikke være null her" }
        assertEquals(Autorisasjon.LESE, p11_3.autorisasjon)

        val p11_4 = requireNotNull(frontendView.sak.paragraf_11_4) { "Paragraf 11-4 skal ikke være null her" }
        assertEquals(Autorisasjon.LESE, p11_4.autorisasjon)

        val p11_5 = requireNotNull(frontendView.sak.paragraf_11_5) { "Paragraf 11-5 skal ikke være null her" }
        assertEquals(Autorisasjon.ENDRE, p11_5.autorisasjon)

        val p11_6 = requireNotNull(frontendView.sak.paragraf_11_6) { "Paragraf 11-6 skal ikke være null her" }
        assertEquals(Autorisasjon.LESE, p11_6.autorisasjon)

        val p11_12 = requireNotNull(frontendView.sak.paragraf_11_12) { "Paragraf 11-12 skal ikke være null her" }
        assertEquals(Autorisasjon.LESE, p11_12.autorisasjon)

        val p11_29 = requireNotNull(frontendView.sak.paragraf_11_29) { "Paragraf 11-29 skal ikke være null her" }
        assertEquals(Autorisasjon.LESE, p11_29.autorisasjon)
    }

}
