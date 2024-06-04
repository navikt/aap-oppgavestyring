package tilgangsstyring

import io.mockk.every
import io.mockk.mockk
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.config.security.OppgavePrincipal
import oppgavestyring.oppgave.db.Oppgave
import oppgavestyring.tilgangsstyring.TilgangstyringService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TilgangstyringServiceTest {

    val tilgangstyringService = TilgangstyringService

    @Test
    fun `saksbehandler kan se saksbehandleroppgave`() {

        val oppgave = mockk<Oppgave>()
        every { oppgave.avklaringsbehovtype } returns Avklaringsbehovtype.AVKLAR_STUDENT
        val principal = mockk<OppgavePrincipal>()
        every { principal.isSaksbehandler() } returns true
        every { principal.isVeileder() } returns false

        val actual = tilgangstyringService.kanSaksbehandlerSeOppgave(principal, oppgave)

        assertTrue(actual)
    }

    @Test
    fun `saksbehandler kan ikke se veilederoppgave`() {

        val oppgave = mockk<Oppgave>()
        every { oppgave.avklaringsbehovtype } returns Avklaringsbehovtype.AVKLAR_SYKDOM
        val principal = mockk<OppgavePrincipal>()
        every { principal.isSaksbehandler() } returns true
        every { principal.isVeileder() } returns false


        val actual = tilgangstyringService.kanSaksbehandlerSeOppgave(principal, oppgave)

        assertFalse(actual)
    }

    @Test
    fun `veileder kan se veilederoppgave`() {

        val oppgave = mockk<Oppgave>()
        every { oppgave.avklaringsbehovtype } returns Avklaringsbehovtype.AVKLAR_SYKDOM
        val principal = mockk<OppgavePrincipal>()
        every { principal.isSaksbehandler() } returns false
        every { principal.isVeileder() } returns true

        val actual = tilgangstyringService.kanSaksbehandlerSeOppgave(principal, oppgave)

        assertTrue(actual)
    }

    @Test
    fun `veileder kan ikke se saksbehandleroppgaver`() {

        val oppgave = mockk<Oppgave>()
        every { oppgave.avklaringsbehovtype } returns Avklaringsbehovtype.AVKLAR_STUDENT
        val principal = mockk<OppgavePrincipal>()
        every { principal.isSaksbehandler() } returns false
        every { principal.isVeileder() } returns true

        val actual = tilgangstyringService.kanSaksbehandlerSeOppgave(principal, oppgave)

        assertFalse(actual)
    }

}