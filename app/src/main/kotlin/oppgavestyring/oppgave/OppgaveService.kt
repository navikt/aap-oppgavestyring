package oppgavestyring.oppgave

import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.oppgave.api.OppgaveParams
import oppgavestyring.oppgave.api.generateOppgaveFilter
import oppgavestyring.oppgave.api.generateOppgaveSorting
import oppgavestyring.oppgave.db.Oppgave
import oppgavestyring.oppgave.db.OppgaveTabell
import oppgavestyring.oppgave.db.Tildelt
import oppgavestyring.tilgangsstyring.GruppeMap
import oppgavestyring.tilgangsstyring.TilgangstyringService
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

    fun søk(gruppe: GruppeMap, searchParams: OppgaveParams): List<Oppgave> {
        val filters = generateOppgaveFilter(searchParams)

        return Oppgave.find {
            filters
        }.orderBy(
            *generateOppgaveSorting(searchParams)
        ).filter { TilgangstyringService.kanSaksbehandlerSeOppgave(gruppe, it) }
    }


    fun hentÅpneOppgaver(gruppe: GruppeMap): List<Oppgave> {
        return Oppgave.find { OppgaveTabell.status eq Avklaringsbehovstatus.OPPRETTET }
            .filter { TilgangstyringService.kanSaksbehandlerSeOppgave(gruppe, it) }
    }

    fun hent(oppgaveId: OppgaveId): Oppgave {
        return Oppgave[oppgaveId]
    }

    fun lukkOppgaverPåBehandling(behandlingsreferanse: Behandlingsreferanse) {
        Oppgave.find { OppgaveTabell.behandlingsreferanse eq behandlingsreferanse }
            .forEach { it.lukkOppgave() }
    }

}