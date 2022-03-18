package no.nav.aap.app.dao

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.dao.SakDao.SakRow.Companion.opprett
import no.nav.aap.app.dao.SakDao.SakRow.Companion.parse
import no.nav.aap.app.db.DBOppgave
import no.nav.aap.app.db.DBSak
import org.intellij.lang.annotations.Language
import java.util.*
import javax.sql.DataSource

internal class SakDao(private val dataSource: DataSource) {

    internal fun insert(sak: DBSak) {
        sessionOf(dataSource).use { session ->
            session.transaction { tSession ->
                sak.insert(tSession)
            }
        }
    }

    internal fun select(personident: String): List<DBSak> {
        return sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                SELECT personident,
                       s.saksid AS saksid,
                       diskresjonskode,
                       egen_ansatt,
                       lokalkontor_enhetsnummer,
                       o.oppgaveid AS oppgaveid,
                       status,
                       nay_eller_kontor,
                       rolle
                FROM sak s
                         INNER JOIN oppgave o on s.saksid = o.saksid
                         INNER JOIN rolle r on o.oppgaveid = r.oppgaveid
            """
            session.run(
                queryOf(query)
                    .map { row -> opprett(row) }
                    .asList
            )
        }.parse()
    }

    private class SakRow(
        private val personident: String,
        private val saksid: UUID,
        private val diskresjonskode: String,
        private val egenAnsatt: Boolean,
        private val lokalkontorEnhetsnummer: String,
        private val oppgaveid: UUID,
        private val status: String,
        private val nayEllerKontor: String,
        private val rolle: String
    ) {
        companion object {
            fun opprett(row: Row) = SakRow(
                personident = row.string("personident"),
                saksid = row.uuid("saksid"),
                diskresjonskode = row.string("diskresjonskode"),
                egenAnsatt = row.boolean("egen_ansatt"),
                lokalkontorEnhetsnummer = row.string("lokalkontor_enhetsnummer"),
                oppgaveid = row.uuid("oppgaveid"),
                status=row.string("status"),
                nayEllerKontor = row.string("nay_eller_kontor"),
                rolle = row.string("rolle")
            )

            fun Iterable<SakRow>.parse(): List<DBSak> {
                return this
                    .groupBy { it.saksid }
                    .values
                    .map { it.parseSaken() }
            }

            private fun Iterable<SakRow>.parseSaken(): DBSak {
                val oppgaver = this
                    .groupBy { it.oppgaveid }
                    .values
                    .map { it.parseOppgaven() }
                return DBSak(
                    personident = first().personident,
                    saksid = first().saksid,
                    diskresjonskode = first().diskresjonskode,
                    egenAnsatt = first().egenAnsatt,
                    lokalkontorEnhetsnummer = first().lokalkontorEnhetsnummer,
                    oppgaver = oppgaver
                )
            }

            private fun Iterable<SakRow>.parseOppgaven(): DBOppgave {
                val roller = map { it.rolle }
                return DBOppgave(
                    oppgaveid = first().oppgaveid,
                    status = first().status,
                    nayEllerKontor = first().nayEllerKontor,
                    roller = roller
                )
            }
        }
    }
}