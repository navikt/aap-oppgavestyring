package oppgavestyring.ekstern.behandlingsflyt

import oppgavestyring.ekstern.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.ekstern.behandlingsflyt.dto.BehandlingshistorikkRequest
import oppgavestyring.intern.oppgave.OppgaveService


class BehandlingsflytAdapter(
    private val oppgaveService: OppgaveService
) {


    fun mapBehandlingshistorikkTilOppgaveHendelser(
        behandling: BehandlingshistorikkRequest
    ) {

        oppgaveService.lukkOppgaverPåBehandling(behandling.referanse)

        val åpentAvklaringsbehov = behandling.getÅpentAvklaringsbehov()
        if (åpentAvklaringsbehov != null) oppgaveService.opprett(
            personident = behandling.personident,
            saksnummer = behandling.saksnummer,
            avklaringsbehovtype = Avklaringsbehovtype.fraKode(åpentAvklaringsbehov.definisjon.type),
            behandlingsreferanse = behandling.referanse,
            behandlingstype = behandling.behandlingType,
            avklaringsbehovOpprettetTidspunkt = åpentAvklaringsbehov.getOpprettelsestidspunkt(),
            behandlingOpprettetTidspunkt = behandling.opprettetTidspunkt
        )

    }

}
