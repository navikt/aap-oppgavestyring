package oppgavestyring.oppgave

import io.ktor.server.plugins.*
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.config.security.OppgavePrincipal
import oppgavestyring.oppgave.api.OppgaveParams
import oppgavestyring.oppgave.api.generateOppgaveFilter
import oppgavestyring.oppgave.api.generateOppgaveSorting
import oppgavestyring.oppgave.db.Oppgave
import oppgavestyring.oppgave.db.OppgaveTabell
import oppgavestyring.oppgave.db.Tildelt
import oppgavestyring.oppgave.db.TildeltTabell
import oppgavestyring.tilgangsstyring.TilgangstyringService
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.vendors.ForUpdateOption
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
            ident = navIdent
            this.oppgave = oppgave
        }
    }

    fun søk(principal: OppgavePrincipal, searchParams: OppgaveParams): List<Oppgave> {
        val filters = generateOppgaveFilter(searchParams)

        return (OppgaveTabell leftJoin TildeltTabell)
            .select(OppgaveTabell.columns)
            .where { filters }
            .orderBy(*generateOppgaveSorting(searchParams))
            .map { Oppgave.wrapRow(it) }
            .filter { TilgangstyringService.kanSaksbehandlerSeOppgave(principal, it) }
    }


    fun hentÅpneOppgaver(principal: OppgavePrincipal): List<Oppgave> {
        return Oppgave.find { OppgaveTabell.status eq Avklaringsbehovstatus.OPPRETTET }
            .filter { TilgangstyringService.kanSaksbehandlerSeOppgave(principal, it) }
    }

    fun hent(oppgaveId: OppgaveId): Oppgave {
        return Oppgave[oppgaveId]
    }

    fun lukkOppgaverPåBehandling(behandlingsreferanse: Behandlingsreferanse) {
        Oppgave.find { OppgaveTabell.behandlingsreferanse eq behandlingsreferanse }
            .forEach { it.lukkOppgave() }
    }

    fun hentNesteOppgave(principal: OppgavePrincipal, searchParams: OppgaveParams): Oppgave {
        val oppgaveFilter = generateOppgaveFilter(searchParams)
        val oppgaveSortering = generateOppgaveSorting(searchParams)

        val nesteOppgave =  (OppgaveTabell leftJoin TildeltTabell)
            .select(OppgaveTabell.columns)
            .forUpdate(ForUpdateOption.PostgreSQL.ForUpdate(ofTables = arrayOf(OppgaveTabell)))
            .where { oppgaveFilter and
                    (OppgaveTabell.status eq Avklaringsbehovstatus.OPPRETTET and
                            (TildeltTabell.ident.isNull())) }
            .orderBy(*oppgaveSortering)
            .map { Oppgave.wrapRow(it) }
            .firstOrNull { TilgangstyringService.kanSaksbehandlerSeOppgave(principal, it) } ?: throw NotFoundException("Ingen flere ledige oppgaver")
        Tildelt.new {
            ident = principal.ident
            oppgave = nesteOppgave
        }
        return nesteOppgave
    }

}