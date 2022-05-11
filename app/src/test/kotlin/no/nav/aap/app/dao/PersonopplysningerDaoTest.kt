package no.nav.aap.app.dao

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.frontendView.FrontendPersonopplysninger
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class PersonopplysningerDaoTest {

    private val postgres = PostgreSQLContainer<Nothing>("postgres:14")
    private lateinit var dataSource: DataSource
    private lateinit var flyway: Flyway

    private lateinit var personopplysningerDao: PersonopplysningerDao

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

        flyway = Flyway.configure().dataSource(dataSource).load()
        personopplysningerDao = PersonopplysningerDao(dataSource)
    }

    @BeforeEach
    internal fun beforeEach() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun `Lagrer personopplysninger i database`() {
        val personopplysninger = FrontendPersonopplysninger(
            personident = "12345678910",
            norgEnhetId = "4201",
            adressebeskyttelse = "UGRADERT",
            geografiskTilknytning = "030101", // bydel
            erSkjermet = false,
            erSkjermetFom = null,
            erSkjermetTom = null,
        )

        personopplysningerDao.insert(personopplysninger)

        assertEquals(1, rowCount("personopplysninger"))

        val personopplysningResult = personopplysningerDao.select("12345678910")

        assertEquals(personopplysninger, personopplysningResult)
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
