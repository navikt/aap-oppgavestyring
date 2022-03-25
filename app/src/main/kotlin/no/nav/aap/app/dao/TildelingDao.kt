package no.nav.aap.app.dao

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.db.DBTildeling
import org.intellij.lang.annotations.Language
import java.util.UUID
import javax.sql.DataSource

internal class TildelingDao(private val dataSource: DataSource) {

    internal fun insert(tildeling: DBTildeling) {
        sessionOf(dataSource).use { session ->
            session.transaction { tSession ->
                @Language("PostgreSQL")
                val query = """
                    INSERT INTO tildeling VALUES(:saksid, :ident, :rolle)
                    ON CONFLICT ON CONSTRAINT unique_tildeling DO NOTHING
                    """
                tSession.run(
                    queryOf(
                        query, mapOf(
                            "saksid" to tildeling.saksid,
                            "ident" to tildeling.ident,
                            "rolle" to tildeling.rolle
                        )
                    ).asUpdate
                )
            }
        }
    }

    internal fun delete(saksid: UUID, ident: String) {
        sessionOf(dataSource).use { session ->
            session.transaction { tSession ->
                @Language("PostgreSQL")
                val query = """
                    DELETE FROM tildeling WHERE saksid = :saksid AND ident = :ident
                    """
                tSession.run(
                    queryOf(
                        query, mapOf(
                            "saksid" to saksid,
                            "ident" to ident
                        )
                    ).asUpdate
                )
            }
        }
    }

}