package no.nav.aap.app.db

import kotliquery.Session
import kotliquery.queryOf
import org.intellij.lang.annotations.Language
import java.util.*

data class DBSak(
    private val personident: String,
    private val saksid: UUID,
    private val diskresjonskode: String,
    private val skjermet: Boolean,
    private val lokalkontorEnhetsnummer: String,
    private val oppgaver: List<DBOppgave>
) {
    internal fun insert(session: Session) {
        @Language("PostgreSQL")
        val query = """
                INSERT INTO sak (personident, saksid, diskresjonskode, skjermet, lokalkontor_enhetsnummer)
                VALUES (:personident, :saksid, :diskresjonskode, :skjermet, :lokalkontor_enhetsnummer)
                ON CONFLICT DO NOTHING
                """
        session.run(
            queryOf(
                query, mapOf(
                    "personident" to personident,
                    "saksid" to saksid,
                    "diskresjonskode" to diskresjonskode,
                    "skjermet" to skjermet,
                    "lokalkontor_enhetsnummer" to lokalkontorEnhetsnummer
                )
            ).asUpdate
        )
        oppgaver.forEach { it.insert(session, saksid) }
    }
}
