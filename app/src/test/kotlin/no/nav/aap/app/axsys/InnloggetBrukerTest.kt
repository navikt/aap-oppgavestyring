package no.nav.aap.app.axsys

import no.nav.aap.app.RoleName
import no.nav.aap.app.frontendView.Autorisasjon
import no.nav.aap.app.frontendView.Utfall
import no.nav.aap.app.kafka.SøkereKafkaDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class InnloggetBrukerTest {

    @Test
    fun `Innlogget bruker ikke tilknyttet NAY får bare lese`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.VEILEDER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = null,
            godkjentAv = null,
            paragraf = "PARAGRAF_11_2",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.IKKE_VURDERT
        )

        val aut = innloggetBruker.hentAutorisasjonForNAY(vilkårsvurdering)
        assertEquals(Autorisasjon.LESE, aut)
    }

    @Test
    fun `Innlogget bruker tilknyttet NAY med SAKSBEEHANDLER rolle får endre`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.SAKSBEHANDLER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = null,
            godkjentAv = null,
            paragraf = "PARAGRAF_11_2",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.IKKE_VURDERT
        )

        val aut = innloggetBruker.hentAutorisasjonForNAY(vilkårsvurdering)
        assertEquals(Autorisasjon.ENDRE, aut)
    }

    @Test
    fun `Innlogget bruker tilknyttet NAY med BESLUTTER rolle får godkjenne`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.BESLUTTER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = null,
            godkjentAv = null,
            paragraf = "PARAGRAF_11_2",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.OPPFYLT
        )

        val aut = innloggetBruker.hentAutorisasjonForNAY(vilkårsvurdering)
        assertEquals(Autorisasjon.GODKJENNE, aut)
    }

    @Test
    fun `Innlogget bruker tilknyttet NAY med BESLUTTER rolle får endre såfremt vilkåret ikke er vurdert`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.BESLUTTER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = null,
            godkjentAv = null,
            paragraf = "PARAGRAF_11_2",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.IKKE_VURDERT
        )

        val aut = innloggetBruker.hentAutorisasjonForNAY(vilkårsvurdering)
        assertEquals(Autorisasjon.ENDRE, aut)
    }

    @Test
    fun `Innlogget bruker tilknyttet NAY med BESLUTTER rolle får endre såfremt vilkåret er vurdert og endret av samme bruker, men får ikke godkjenne`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.BESLUTTER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = "test@test.com",
            godkjentAv = null,
            paragraf = "PARAGRAF_11_2",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.OPPFYLT
        )

        val aut = innloggetBruker.hentAutorisasjonForNAY(vilkårsvurdering)
        assertEquals(Autorisasjon.ENDRE, aut)
    }

    @Test
    fun `Innlogget bruker tilknyttet NAY med BESLUTTER rolle får godkjenne såfremt vilkåret er vurdert og endret av annen bruker`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.BESLUTTER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = "test2@test.com",
            godkjentAv = null,
            paragraf = "PARAGRAF_11_2",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.OPPFYLT
        )

        val aut = innloggetBruker.hentAutorisasjonForNAY(vilkårsvurdering)
        assertEquals(Autorisasjon.GODKJENNE, aut)
    }

    @Test
    fun `Innlogget bruker ikke tilknyttet lokalkontor får bare lese`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.SAKSBEHANDLER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = null,
            godkjentAv = null,
            paragraf = "PARAGRAF_11_5",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.IKKE_VURDERT
        )

        val aut = innloggetBruker.hentAutorisasjonForLokalkontor(vilkårsvurdering)
        assertEquals(Autorisasjon.LESE, aut)
    }

    @Test
    fun `Innlogget bruker tilknyttet lokalkontor med VEILEDER rolle får endre`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.VEILEDER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = null,
            godkjentAv = null,
            paragraf = "PARAGRAF_11_5",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.IKKE_VURDERT
        )

        val aut = innloggetBruker.hentAutorisasjonForLokalkontor(vilkårsvurdering)
        assertEquals(Autorisasjon.ENDRE, aut)
    }

    @Test
    fun `Innlogget bruker tilknyttet lokalkontor med FATTER rolle får godkjenne`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.FATTER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = null,
            godkjentAv = null,
            paragraf = "PARAGRAF_11_5",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.OPPFYLT
        )

        val aut = innloggetBruker.hentAutorisasjonForLokalkontor(vilkårsvurdering)
        assertEquals(Autorisasjon.GODKJENNE, aut)
    }

    @Test
    fun `Innlogget bruker tilknyttet lokalkontor med FATTER rolle får endre såfremt vilkåret ikke er vurdert`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.FATTER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = null,
            godkjentAv = null,
            paragraf = "PARAGRAF_11_5",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.IKKE_VURDERT
        )

        val aut = innloggetBruker.hentAutorisasjonForLokalkontor(vilkårsvurdering)
        assertEquals(Autorisasjon.ENDRE, aut)
    }

    @Test
    fun `Innlogget bruker tilknyttet lokalkontor med FATTER rolle får endre såfremt vilkåret er vurdert og endret av samme bruker, men får ikke godkjenne`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.FATTER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = "test@test.com",
            godkjentAv = null,
            paragraf = "PARAGRAF_11_5",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.OPPFYLT
        )

        val aut = innloggetBruker.hentAutorisasjonForLokalkontor(vilkårsvurdering)
        assertEquals(Autorisasjon.ENDRE, aut)
    }

    @Test
    fun `Innlogget bruker tilknyttet lokalkontor med FATTER rolle får godkjenne såfremt vilkåret er vurdert og endret av annen bruker`() {
        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.FATTER),
            tilknyttedeEnheter = listOf()
        )

        val vilkårsvurdering = SøkereKafkaDto.Vilkårsvurdering(
            vilkårsvurderingsid = UUID.randomUUID(),
            vurdertAv = "test2@test.com",
            godkjentAv = null,
            paragraf = "PARAGRAF_11_5",
            ledd = listOf("LEDD_1"),
            tilstand = "SOKNAD_MOTTATT",
            utfall = Utfall.OPPFYLT
        )

        val aut = innloggetBruker.hentAutorisasjonForLokalkontor(vilkårsvurdering)
        assertEquals(Autorisasjon.GODKJENNE, aut)
    }
}