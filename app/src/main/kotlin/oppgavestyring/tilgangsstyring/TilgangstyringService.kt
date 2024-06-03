package oppgavestyring.tilgangsstyring

import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.oppgave.NavIdent
import oppgavestyring.oppgave.db.Oppgave

object TilgangstyringService {

    private val nayIdent = "Z994020"

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
    fun kanSaksbehandlerSeOppgave(navIdent: NavIdent,  oppgave: Oppgave): Boolean {
        if (nayIdent == navIdent.asString() && oppgave.avklaringsbehovtype in nayOppgaver) {
            return true
        } else if (nayIdent != navIdent.asString() && oppgave.avklaringsbehovtype !in nayOppgaver) {
            return true
        }
        return false
    }

}