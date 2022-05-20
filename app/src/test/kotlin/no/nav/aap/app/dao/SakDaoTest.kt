package no.nav.aap.app.dao

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.dao.InitTestDatabase.dataSource
import no.nav.aap.app.db.DBOppgave
import no.nav.aap.app.db.DBSak
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

internal class SakDaoTest : DatabaseTestBase() {
    private val sakDao = SakDao(dataSource)

    @Test
    fun `Lagrer dbsak i database`() {
        val dbSak = DBSak(
            personident = "12345678910",
            saksid = UUID.randomUUID(),
            diskresjonskode = "UGRADERT",
            skjermet = false,
            lokalkontorEnhetsnummer = "030102",
            oppgaver = listOf(
                DBOppgave(
                    oppgaveid = UUID.randomUUID(),
                    status = "IKKE_VURDERT",
                    nayEllerKontor = "KONTOR",
                    roller = listOf("BEHANDLER")
                )
            )
        )
        sakDao.insert(dbSak)

        val actual = sakDao.select("12345678910")

        assertEquals(dbSak, actual.first())
    }

    @Test
    fun `Oppdaterer status på oppgave ved endring av sak`() {
        val saksid = UUID.randomUUID()
        val oppgaveid = UUID.randomUUID()
        val initiellSak = DBSak(
            personident = "12345678910",
            saksid = saksid,
            diskresjonskode = "UGRADERT",
            skjermet = false,
            lokalkontorEnhetsnummer = "030102",
            oppgaver = listOf(
                DBOppgave(
                    oppgaveid = oppgaveid,
                    status = "IKKE_VURDERT",
                    nayEllerKontor = "KONTOR",
                    roller = listOf("BEHANDLER")
                )
            )
        )

        val oppdatert = DBSak(
            personident = "12345678910",
            saksid = saksid,
            diskresjonskode = "UGRADERT",
            skjermet = false,
            lokalkontorEnhetsnummer = "030102",
            oppgaver = listOf(
                DBOppgave(
                    oppgaveid = oppgaveid,
                    status = "BEHANDLET",
                    nayEllerKontor = "KONTOR",
                    roller = listOf("BEHANDLER")
                )
            )
        )
        sakDao.insert(initiellSak)
        sakDao.insert(oppdatert)

        val actual = sakDao.select("12345678910")

        assertEquals(oppdatert, actual.first())

        assertEquals(1, rowCount("sak"))
        assertEquals(1, rowCount("oppgave"))
        assertEquals(1, rowCount("rolle"))
    }

    @Test
    fun `Sletter dbsak fra database`() {
        val dbSak = DBSak(
            personident = "12345678910",
            saksid = UUID.randomUUID(),
            diskresjonskode = "UGRADERT",
            skjermet = false,
            lokalkontorEnhetsnummer = "030102",
            oppgaver = listOf(
                DBOppgave(
                    oppgaveid = UUID.randomUUID(),
                    status = "IKKE_VURDERT",
                    nayEllerKontor = "KONTOR",
                    roller = listOf("BEHANDLER")
                )
            )
        )
        sakDao.insert(dbSak)

        assertEquals(1, rowCount("sak"))
        assertEquals(1, rowCount("oppgave"))
        assertEquals(1, rowCount("rolle"))

        sakDao.delete("12345678910")

        assertEquals(0, rowCount("sak"))
        assertEquals(0, rowCount("oppgave"))
        assertEquals(0, rowCount("rolle"))
    }

    @Test
    fun `Sletter ikke dbsak til annen personident fra database`() {
        sakDao.insert(
            DBSak(
                personident = "01987654321",
                saksid = UUID.randomUUID(),
                diskresjonskode = "UGRADERT",
                skjermet = false,
                lokalkontorEnhetsnummer = "030102",
                oppgaver = listOf(
                    DBOppgave(
                        oppgaveid = UUID.randomUUID(),
                        status = "IKKE_VURDERT",
                        nayEllerKontor = "KONTOR",
                        roller = listOf("BEHANDLER")
                    )
                )
            )
        )

        assertEquals(1, rowCount("sak"))
        assertEquals(1, rowCount("oppgave"))
        assertEquals(1, rowCount("rolle"))

        sakDao.insert(
            DBSak(
                personident = "12345678910",
                saksid = UUID.randomUUID(),
                diskresjonskode = "UGRADERT",
                skjermet = false,
                lokalkontorEnhetsnummer = "030102",
                oppgaver = listOf(
                    DBOppgave(
                        oppgaveid = UUID.randomUUID(),
                        status = "IKKE_VURDERT",
                        nayEllerKontor = "KONTOR",
                        roller = listOf("BEHANDLER")
                    )
                )
            )
        )

        assertEquals(2, rowCount("sak"))
        assertEquals(2, rowCount("oppgave"))
        assertEquals(2, rowCount("rolle"))

        sakDao.delete("01987654321")

        assertEquals(1, rowCount("sak"))
        assertEquals(1, rowCount("oppgave"))
        assertEquals(1, rowCount("rolle"))
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
