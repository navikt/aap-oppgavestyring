package oppgavestyring.oppgave

import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.oppgave.api.OppgaveParams
import oppgavestyring.oppgave.db.Oppgave
import oppgavestyring.oppgave.db.OppgaveTabell
import oppgavestyring.oppgave.db.Tildelt
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

typealias Behandlingsreferanse = String
typealias Saksnummer = String

class OppgaveService {

    fun opprett(
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

    fun søk(searchParams: OppgaveParams<OppgaveTabell>): SizedIterable<Oppgave> {
        val yolo = searchParams.sorting.entries.first()
        val op = Oppgave
        val ll = searchParams.filters.map {
            it.key.get(OppgaveTabell).eq(it.value)
        }.toTypedArray()
        val fggg = SqlExpressionBuilder.concat(*ll)
        return Oppgave.find { fggg }

            .orderBy(*searchParams.sorting.map { it.key.get(OppgaveTabell) to it.value }.toTypedArray())
    }

    fun hentÅpneOppgaver(): SizedIterable<Oppgave> {
        return Oppgave.find { OppgaveTabell.status eq Avklaringsbehovstatus.OPPRETTET }
    }

    fun hent(oppgaveId: OppgaveId): Oppgave {
        return Oppgave[oppgaveId]
    }

    fun lukkOppgaverPåBehandling(behandlingsreferanse:  Behandlingsreferanse) {
        Oppgave.find { OppgaveTabell.behandlingsreferanse eq behandlingsreferanse }
            .forEach { it.lukkOppgave() }
    }

}