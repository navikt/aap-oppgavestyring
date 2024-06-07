package oppgavestyring.ekstern.behandlingsflyt

import oppgavestyring.ekstern.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.ekstern.behandlingsflyt.dto.BehandlingshistorikkRequest
import oppgavestyring.intern.oppgave.OppgaveService


class BehandlingsflytAdapter(
    private val oppgaveService: OppgaveService
) {


    fun mapBehnadlingshistorikkTilOppgaveHendelser(
        behanlding: BehandlingshistorikkRequest
    ) {

        oppgaveService.lukkOppgaverPåBehandling(behanlding.referanse)

        val åpentAvklaringsbehov = behanlding.getÅpentAvklaringsbehov()
        if (åpentAvklaringsbehov != null) oppgaveService.opprett(
            personident = behanlding.personident,
            saksnummer = behanlding.saksnummer,
            avklaringsbehovtype = Avklaringsbehovtype.fraKode(åpentAvklaringsbehov.definisjon.type),
            behandlingsreferanse = behanlding.referanse,
            behandlingstype = behanlding.behandlingType,
            avklaringsbehovOpprettetTidspunkt = åpentAvklaringsbehov.getOpprettelsestidspunkt(),
            behandlingOpprettetTidspunkt = behanlding.opprettetTidspunkt
        )

    }

}
