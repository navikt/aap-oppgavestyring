package oppgavestyring.tilgangsstyring

import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.config.security.AdGruppe
import oppgavestyring.oppgave.db.Oppgave

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
    fun kanSaksbehandlerSeOppgave(gruppe: AdGruppe, oppgave: Oppgave): Boolean {
        if (AdGruppe.NAY == gruppe && oppgave.avklaringsbehovtype in nayOppgaver) {
            return true
        } else if (AdGruppe.KONTOR == gruppe && oppgave.avklaringsbehovtype !in nayOppgaver) {
            return true
        }
        return false
    }

}