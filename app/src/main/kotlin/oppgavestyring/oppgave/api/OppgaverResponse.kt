package oppgavestyring.oppgave.api


import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import java.time.LocalDateTime

data class OppgaverResponse(
    val oppgaver: List<Oppgave>,
)

data class Oppgave(
    val oppgaveId: Long,
    val avklaringsbehov: Avklaringsbehovtype,
    val status: Avklaringsbehovstatus,
    val foedselsnummer: String, //innbygger
    val avklaringsbehovOpprettetTid: LocalDateTime,
    val behandlingOpprettetTid: LocalDateTime,
    val tilordnetRessurs: String? = null,
    val reservertTil: String? = null
)
