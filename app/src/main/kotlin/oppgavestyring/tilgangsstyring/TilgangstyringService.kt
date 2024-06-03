package oppgavestyring.tilgangsstyring

import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
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
    fun kanSaksbehandlerSeOppgave(gruppe: GruppeMap, oppgave: Oppgave): Boolean {
        if (GruppeMap.NAY == gruppe && oppgave.avklaringsbehovtype in nayOppgaver) {
            return true
        } else if (GruppeMap.KONTOR == gruppe && oppgave.avklaringsbehovtype !in nayOppgaver) {
            return true
        }
        return false
    }

}