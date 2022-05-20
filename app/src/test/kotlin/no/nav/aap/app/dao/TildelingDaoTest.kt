package no.nav.aap.app.dao

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.dao.InitTestDatabase.dataSource
import no.nav.aap.app.db.DBTildeling
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class TildelingDaoTest : DatabaseTestBase() {
    private val tildelingDao = TildelingDao(dataSource)

    @Test
    fun `Insert i tabell`() {
        val tildeling = DBTildeling(
            saksid = UUID.randomUUID(),
            ident = "Z123456",
            rolle = "BEHANDLER"
        )

        tildelingDao.insert(tildeling)

        assertEquals(1, rowCount("tildeling"))
    }

    @Test
    fun `Delete fra tabell`() {
        val uuid = UUID.randomUUID()
        val ident = "Z123456"
        val tildeling = DBTildeling(
            saksid = uuid,
            ident = ident,
            rolle = "BEHANDLER"
        )

        tildelingDao.insert(tildeling)

        assertEquals(1, rowCount("tildeling"))

        tildelingDao.delete(uuid, ident)

        assertEquals(0, rowCount("tildeling"))
    }

    private fun rowCount(tabell: String): Int {
        @Language("PostgreSQL")
        val query = """
            SELECT count(1) FROM $tabell
        """
        return sessionOf(dataSource).use { session ->
            session.run(queryOf(query).map { row -> row.int(1) }.asSingle)!!
        }
    }
}