package oppgavestyring.intern.oppgave

import io.ktor.server.plugins.*
import oppgavestyring.config.security.OppgavePrincipal
import oppgavestyring.ekstern.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.ekstern.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.ekstern.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.ekstern.oppslag.OppslagClient
import oppgavestyring.ekstern.tilgangsstyring.TilgangstyringService
import oppgavestyring.intern.oppgave.api.OppgaveParams
import oppgavestyring.intern.oppgave.api.generateOppgaveFilter
import oppgavestyring.intern.oppgave.api.generateOppgaveSorting
import oppgavestyring.intern.oppgave.db.Oppgave
import oppgavestyring.intern.oppgave.db.OppgaveTabell
import oppgavestyring.intern.oppgave.db.Tildelt
import oppgavestyring.intern.oppgave.db.TildeltTabell
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.vendors.ForUpdateOption
import java.time.LocalDateTime

typealias Behandlingsreferanse = String
typealias Saksnummer = String

class OppgaveService(private val oppslagClient: OppslagClient) {

    fun opprett(
        personident: String,
        saksnummer: Saksnummer,
        behandlingsreferanse: Behandlingsreferanse,
        behandlingstype: Behandlingstype,
        avklaringsbehovtype: Avklaringsbehovtype,
        avklaringsbehovOpprettetTidspunkt: LocalDateTime,
        behandlingOpprettetTidspunkt: LocalDateTime
    ): Oppgave {
        val fultNavn = oppslagClient.hentNavnForPersonIdent(personident.let(::Personident)).toString()
        return Oppgave.new {
            this.saksnummer = saksnummer
            this.behandlingsreferanse = behandlingsreferanse
            this.behandlingstype = behandlingstype
            this.avklaringsbehovtype = avklaringsbehovtype
            status = Avklaringsbehovstatus.OPPRETTET
            this.avklaringsbehovOpprettetTidspunkt = avklaringsbehovOpprettetTidspunkt
            this.behandlingOpprettetTidspunkt = behandlingOpprettetTidspunkt
            personnummer = personident
            personNavn = fultNavn
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