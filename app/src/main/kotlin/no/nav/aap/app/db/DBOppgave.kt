package no.nav.aap.app.db

import kotliquery.Session
import kotliquery.queryOf
import org.intellij.lang.annotations.Language
import java.util.*

data class DBOppgave(
    private val oppgaveid: UUID,
    private val status: String,
    private val nayEllerKontor: String,
    private val roller: List<String>
) {
    internal fun insert(session: Session, saksid: UUID) {
        @Language("PostgreSQL")
        val query = """
                INSERT INTO oppgave (saksid, oppgaveid, nay_eller_kontor, status)
                VALUES (:saksid, :oppgaveid, :nay_eller_kontor, :status)
                ON CONFLICT (oppgaveid) DO UPDATE SET status = :status
                """
        session.run(
            queryOf(
                query, mapOf(
                    "saksid" to saksid,
                    "oppgaveid" to oppgaveid,
                    "nay_eller_kontor" to nayEllerKontor,
                    "status" to status
                )
            ).asUpdate
        )
        roller.forEach { rolle -> insertRolle(session, rolle) }
    }

    private fun insertRolle(session: Session, rolle: String) {
        @Language("PostgreSQL")
        val query = """
                INSERT INTO rolle (oppgaveid, rolle)
                VALUES (:oppgaveid, :rolle)
                ON CONFLICT ON CONSTRAINT unique_rolle DO NOTHING
                """
        session.run(
            queryOf(
                query, mapOf(
                    "oppgaveid" to oppgaveid,
                    "rolle" to rolle
                )
            ).asUpdate
        )
    }
}
