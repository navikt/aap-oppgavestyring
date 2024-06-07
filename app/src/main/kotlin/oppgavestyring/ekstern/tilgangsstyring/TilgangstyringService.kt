package oppgavestyring.ekstern.tilgangsstyring

import oppgavestyring.config.security.OppgavePrincipal
import oppgavestyring.ekstern.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.intern.oppgave.db.Oppgave

object TilgangstyringService {

    private val nayOppgaver = listOf(
        Avklaringsbehovtype.AVKLAR_STUDENT,
        Avklaringsbehovtype.FASTSETT_BEREGNINGSTIDSPUNKT,
        Avklaringsbehovtype.VURDER_SYKEPENGEERSTATNING,
        Avklaringsbehovtype.FORESLÅ_VEDTAK,
        Avklaringsbehovtype.FATTE_VEDTAK
    )


    // spør tilgangstyring videre på vegne av brukerkall
    fun kanSaksbehandlerSeOppgave(oppgave: Oppgave) {

    }

    // spør oppgavestyring med ident ikke fra token, feks ved tildeling av oppgave til andre
    fun kanSaksbehandlerSeOppgave(principal: OppgavePrincipal, oppgave: Oppgave): Boolean {
        if (principal.isSaksbehandler() && oppgave.avklaringsbehovtype in nayOppgaver) {
            return true
        }

        if (principal.isVeileder() && oppgave.avklaringsbehovtype !in nayOppgaver) {
            return true
        }
        return false
    }

}