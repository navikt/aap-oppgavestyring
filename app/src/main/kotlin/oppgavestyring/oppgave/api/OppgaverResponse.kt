package oppgavestyring.oppgave.api


import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.oppgave.db.Oppgave
import java.time.LocalDateTime

data class OppgaverResponse(
    val oppgaver: List<OppgaveDto>,
)

data class OppgaveDto(
    val oppgaveId: Long,
    val avklaringsbehov: Avklaringsbehovtype,
    val status: Avklaringsbehovstatus,
    val foedselsnummer: String, //innbygger
    val avklaringsbehovOpprettetTid: LocalDateTime,
    val behandlingOpprettetTid: LocalDateTime,
    val tilordnetRessurs: String? = null,
    val reservertTil: String? = null
) {
    companion object {
        fun fromOppgave(oppgave: Oppgave) = OppgaveDto(
            oppgaveId = oppgave.id.value,
            status = oppgave.status,
            foedselsnummer = oppgave.personnummer,
            tilordnetRessurs = oppgave.tildelt?.ident,
            avklaringsbehov = oppgave.avklaringsbehovtype,
            behandlingOpprettetTid = oppgave.behandlingOpprettetTidspunkt,
            avklaringsbehovOpprettetTid = oppgave.avklaringsbehovOpprettetTidspunkt
        )
    }
}
