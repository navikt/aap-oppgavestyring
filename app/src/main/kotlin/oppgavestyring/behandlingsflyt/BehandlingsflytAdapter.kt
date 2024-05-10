package oppgavestyring.behandlingsflyt

import oppgavestyring.behandlingsflyt.dto.AvklaringsbehovHendelseDto
import oppgavestyring.behandlingsflyt.dto.BehandlingshistorikkRequest
import oppgavestyring.behandlingsflyt.dto.Oppgavestatus
import oppgavestyring.oppgave.OppgaveService
import oppgavestyring.oppgave.Personident
import oppgavestyring.oppgave.adapter.Token


class BehandlingsflytAdapter(
    private val oppgaveService: OppgaveService
) {


    suspend fun mapBehnadlingshistorikkTilOppgaveHendelser(
        behanlding: BehandlingshistorikkRequest) {

        if (behanlding.getÅpentAvklaringsbehov() != null) oppgaveService.opprett_v2(
            token = Token("sdfgvsd"),
            beskrivelse = behanlding.getÅpentAvklaringsbehov()!!.type.beskrivelse,
            personident = Personident(behanlding.personident)
        )
        else if (behanlding.erLukket()) {
            oppgaveService.lukkOppgave(behanlding.behandlingsreferanse)
        }

    }

}
