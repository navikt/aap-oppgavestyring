package oppgavestyring.behandlingsflyt

import oppgavestyring.behandlingsflyt.dto.AvklaringsbehovHendelseDto
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.BehandlingshistorikkRequest
import oppgavestyring.behandlingsflyt.dto.Oppgavestatus



data class OpprettOppgave(
    val type: Avklaringsbehovtype,

)

object BehandlingsflytHistorikkRydder {


    fun mapBehnadlingshistorikkTilOppgaveHendelser(
        behanlding: BehandlingshistorikkRequest,
        oppgaver: List<Oppgave>): OpprettOppgave? {

        if (behanlding.erLukket())
            return null

        val åpentAvklaringsbehov = behanlding.getÅpentAvklaringsbehov()

        return if (åpentAvklaringsbehov != null) OpprettOppgave(Avklaringsbehovtype.AVKLAR_STUDENT_KODE) else null

    }

}

data class Oppgave (
    val status: Oppgavestatus,
    val avklaringsbehov: AvklaringsbehovHendelseDto
) {


}