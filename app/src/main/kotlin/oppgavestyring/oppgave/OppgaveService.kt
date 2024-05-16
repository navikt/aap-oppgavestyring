package oppgavestyring.oppgave

import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.oppgave.db.Oppgave
import oppgavestyring.oppgave.db.OppgaveTabell
import oppgavestyring.oppgave.db.Tildelt
import java.time.LocalDateTime

typealias Behandlingsreferanse = String
typealias Saksnummer = String

class OppgaveService {

    fun opprett_v2(
        personident: String,
        saksnummer: Saksnummer,
        behandlingsreferanse: Behandlingsreferanse,
        behandlingstype: Behandlingstype,
        avklaringsbehovtype: Avklaringsbehovtype,
        avklaringsbehovOpprettetTidspunkt: LocalDateTime,
        behandlingOpprettetTidspunkt: LocalDateTime
    ): Oppgave {
        return Oppgave.new {
            this.saksnummer = saksnummer
            this.behandlingsreferanse = behandlingsreferanse
            this.behandlingstype = behandlingstype
            this.avklaringsbehovtype = avklaringsbehovtype
            status = Avklaringsbehovstatus.OPPRETTET
            this.avklaringsbehovOpprettetTidspunkt = avklaringsbehovOpprettetTidspunkt
            this.behandlingOpprettetTidspunkt = behandlingOpprettetTidspunkt
            personnummer = personident
        }
    }

    fun frigiRessursFraOppgave(id: OppgaveId) {
        Oppgave[id].tildelt?.delete()
    }

    fun tildelOppgave(id: OppgaveId, navIdent: NavIdent) {
        val oppgave = Oppgave[id]
        Tildelt.new {
            ident = navIdent.asString()
            this.oppgave = oppgave
        }
    }

    fun søk(): List<Oppgave> {
        return Oppgave.all().toList()
    }

    fun hent(oppgaveId: OppgaveId): Oppgave {
        return Oppgave[oppgaveId]
    }

    fun lukkOppgaverPåBehandling(behandlingsreferanse:  Behandlingsreferanse) {
        Oppgave.find { OppgaveTabell.behandlingsreferanse eq behandlingsreferanse }
            .forEach { it.lukkOppgave() }
    }

}