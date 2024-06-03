package tilgangsstyring

import io.mockk.every
import io.mockk.mockk
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.oppgave.NavIdent
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

        val actual = tilgangstyringService.kanSaksbehandlerSeOppgave(NavIdent("Z994020"), oppgave)

        assertTrue(actual)
    }

    @Test
    fun `saksbehandler kan ikke se veilederoppgave`() {

        val oppgave = mockk<Oppgave>()
        every { oppgave.avklaringsbehovtype } returns Avklaringsbehovtype.AVKLAR_SYKDOM

        val actual = tilgangstyringService.kanSaksbehandlerSeOppgave(NavIdent("Z994020"), oppgave)

        assertFalse(actual)
    }

    @Test
    fun `veileder kan se veilederoppgave`() {

        val oppgave = mockk<Oppgave>()
        every { oppgave.avklaringsbehovtype } returns Avklaringsbehovtype.AVKLAR_SYKDOM

        val actual = tilgangstyringService.kanSaksbehandlerSeOppgave(NavIdent("Z123456"), oppgave)

        assertTrue(actual)
    }

    @Test
    fun `veileder kan ikke se saksbehandleroppgaver`() {

        val oppgave = mockk<Oppgave>()
        every { oppgave.avklaringsbehovtype } returns Avklaringsbehovtype.AVKLAR_STUDENT

        val actual = tilgangstyringService.kanSaksbehandlerSeOppgave(NavIdent("Z123456"), oppgave)

        assertFalse(actual)
    }

}