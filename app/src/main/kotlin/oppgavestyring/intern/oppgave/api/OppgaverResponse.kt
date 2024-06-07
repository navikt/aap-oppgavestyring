package oppgavestyring.intern.oppgave.api


import oppgavestyring.ekstern.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.ekstern.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.ekstern.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.intern.oppgave.Behandlingsreferanse
import oppgavestyring.intern.oppgave.db.Oppgave
import java.time.LocalDateTime

data class OppgaverResponse(
    val oppgaver: List<OppgaveDto>,
)

data class OppgaveDto(
    val oppgaveId: Long,
    val behandlingsreferanse: Behandlingsreferanse,
    val behandlingstype: Behandlingstype,
    val saksnummer: String,
    val avklaringsbehov: Avklaringsbehovtype,
    val status: Avklaringsbehovstatus,
    val foedselsnummer: String, //innbygger
    val avklaringsbehovOpprettetTid: LocalDateTime,
    val behandlingOpprettetTid: LocalDateTime,
    val oppgaveOpprettet: LocalDateTime,
    val tilordnetRessurs: String? = null,
    val reservertTil: String? = null
) {
    companion object {
        fun fromOppgave(oppgave: Oppgave) = OppgaveDto(
            oppgaveId = oppgave.id.value,
            behandlingsreferanse = oppgave.behandlingsreferanse,
            behandlingstype = oppgave.behandlingstype,
            saksnummer = oppgave.saksnummer,
            status = oppgave.status,
            foedselsnummer = oppgave.personnummer,
            tilordnetRessurs = oppgave.tildelt?.ident?.toString(),
            avklaringsbehov = oppgave.avklaringsbehovtype,
            behandlingOpprettetTid = oppgave.behandlingOpprettetTidspunkt,
            avklaringsbehovOpprettetTid = oppgave.avklaringsbehovOpprettetTidspunkt,
            oppgaveOpprettet = oppgave.tidsstempel
        )
    }
}
