package no.nav.aap.app

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.dao.SakDao
import no.nav.aap.app.db.DBOppgave
import no.nav.aap.app.db.DBSak
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*
import javax.sql.DataSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DbTest {

    private val postgres = PostgreSQLContainer<Nothing>("postgres:14")
    private lateinit var dataSource: DataSource
    private lateinit var flyway: Flyway

    private lateinit var sakDao: SakDao

    @BeforeAll
    internal fun beforeAll() {
        postgres.start()
        dataSource = HikariDataSource(HikariConfig().apply {
            jdbcUrl = postgres.jdbcUrl
            username = postgres.username
            password = postgres.password
            maximumPoolSize = 3
            minimumIdle = 1
            initializationFailTimeout = 5000
            idleTimeout = 10001
            connectionTimeout = 1000
            maxLifetime = 30001
        })

        flyway = Flyway
            .configure()
            .dataSource(dataSource)
            .load()

        sakDao = SakDao(dataSource)
    }

    @BeforeEach
    internal fun beforeEach() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun `Lagrer dbsak i database`() {
        val dbSak = DBSak(
            personident = "12345678910",
            saksid = UUID.randomUUID(),
            diskresjonskode = "UGRADERT",
            egenAnsatt = false,
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
            egenAnsatt = false,
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
            egenAnsatt = false,
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
