package no.nav.aap.app.dao

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.db.DBTildeling
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.testcontainers.containers.PostgreSQLContainer
import java.util.UUID
import javax.sql.DataSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TildelingDaoTest {

    private val postgres = PostgreSQLContainer<Nothing>("postgres:14")
    private lateinit var dataSource: DataSource
    private lateinit var flyway: Flyway

    private lateinit var tildelingDao: TildelingDao

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

        tildelingDao = TildelingDao(dataSource)
    }

    @BeforeEach
    internal fun beforeEach() {
        flyway.clean()
        flyway.migrate()
    }

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