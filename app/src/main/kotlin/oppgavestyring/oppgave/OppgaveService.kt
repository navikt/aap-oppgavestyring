package oppgavestyring.oppgave

import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovstatus
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.behandlingsflyt.dto.Behandlingstype
import oppgavestyring.oppgave.api.OppgaveDto
import oppgavestyring.oppgave.api.OppgaveParams
import oppgavestyring.oppgave.db.Oppgave
import oppgavestyring.oppgave.db.OppgaveTabell
import oppgavestyring.oppgave.db.Tildelt
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
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

    fun søk(searchParams: OppgaveParams): SizedIterable<Oppgave> {
        val filters = searchParams.filters.map { filter ->
            when (filter.key) {
                OppgaveDto::behandlingstype.name -> OppgaveTabell::behandlingstype.get()
                    .inList(filter.value.map { Behandlingstype.valueOf(it) })

                OppgaveDto::avklaringsbehov.name -> OppgaveTabell::avklaringbehovtype.get()
                    .inList(filter.value.map { Avklaringsbehovtype.valueOf(it) })

                OppgaveDto::avklaringsbehovOpprettetTid.name -> OppgaveTabell::avklaringsbehovOpprettetTidspunkt.get()
                    .eq(LocalDateTime.parse(filter.value.first()))

                OppgaveDto::behandlingOpprettetTid.name -> OppgaveTabell::behandlingOpprettetTidspunkt.get()
                    .eq(LocalDateTime.parse(filter.value.first()))

                else -> null
            }
        }.filterNotNull()

        return Oppgave.find {
            filters.fold(Op.TRUE.and(Op.TRUE)) { acc, value -> acc.and(value)}
        }.orderBy(
            *searchParams.sorting.map {
                when (it.key) {
                    OppgaveDto::behandlingstype.name -> OppgaveTabell::behandlingstype.get() to it.value
                    OppgaveDto::avklaringsbehov.name -> OppgaveTabell::avklaringbehovtype.get() to it.value
                    OppgaveDto::avklaringsbehovOpprettetTid.name -> OppgaveTabell::avklaringsbehovOpprettetTidspunkt.get() to it.value
                    OppgaveDto::behandlingOpprettetTid.name -> OppgaveTabell::behandlingOpprettetTidspunkt.get() to it.value
                    else -> null
                }
            }.filterNotNull().toTypedArray()
        )
    }

    fun hentÅpneOppgaver(): SizedIterable<Oppgave> {
        return Oppgave.find { OppgaveTabell.status eq Avklaringsbehovstatus.OPPRETTET }
    }

    fun hent(oppgaveId: OppgaveId): Oppgave {
        return Oppgave[oppgaveId]
    }

    fun lukkOppgaverPåBehandling(behandlingsreferanse: Behandlingsreferanse) {
        Oppgave.find { OppgaveTabell.behandlingsreferanse eq behandlingsreferanse }
            .forEach { it.lukkOppgave() }
    }

}