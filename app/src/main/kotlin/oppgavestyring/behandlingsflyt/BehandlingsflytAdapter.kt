package oppgavestyring.behandlingsflyt

import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.BehandlingshistorikkRequest
import oppgavestyring.oppgave.OppgaveService


class BehandlingsflytAdapter(
    private val oppgaveService: OppgaveService
) {


    fun mapBehnadlingshistorikkTilOppgaveHendelser(
        behanlding: BehandlingshistorikkRequest) {

        /**
            Hvis åpent avklaringsbehov, lukk åpen oppgave på behandling, lag ny oppgave

         */
        val åpentAvklaringsbehov = behanlding.getÅpentAvklaringsbehov()
        if (åpentAvklaringsbehov != null) oppgaveService.opprett_v2(
            personident = behanlding.personident,
            avklaringsbehovtype = Avklaringsbehovtype.fraKode(åpentAvklaringsbehov.definisjon.type),
            behandlingsreferanse = behanlding.referanse,
            behandlingstype = behanlding.behandlingType,
            avklaringsbehovOpprettetTidspunkt = åpentAvklaringsbehov.getOpprettelsestidspunkt(),
            behandlingOpprettetTidspunkt = behanlding.opprettetTidspunkt
        )
        else if (behanlding.erLukket()) {
            oppgaveService.lukkOppgave(behanlding.referanse)
        }

    }

}
