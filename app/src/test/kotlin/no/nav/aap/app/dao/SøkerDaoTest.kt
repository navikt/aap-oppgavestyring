package no.nav.aap.app.dao

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.frontendView.*
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import java.time.LocalDate
import java.util.*
import javax.sql.DataSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SøkerDaoTest {

    private val postgres = PostgreSQLContainer<Nothing>("postgres:14")
    private lateinit var dataSource: DataSource
    private lateinit var flyway: Flyway

    private lateinit var søkerDao: SøkerDao

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

        søkerDao = SøkerDao(dataSource)
    }

    @BeforeEach
    internal fun beforeEach() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun `Lagrer liste med frontendsak i database`() {
        val frontendSøker = FrontendSøker(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            skjermet = false,
            sak = FrontendSak(
                saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                type = "11-5",
                paragraf_11_2 = FrontendParagraf_11_2(
                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                    erOppfylt = false,
                    måVurderesManuelt = true
                ),
                paragraf_11_3 = FrontendParagraf_11_3(
                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                    erOppfylt = false,
                    måVurderesManuelt = true
                ),
                paragraf_11_4 = FrontendParagraf_11_4(
                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                    erOppfylt = false,
                    måVurderesManuelt = true
                ),
                paragraf_11_5 = FrontendParagraf_11_5(
                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                    erOppfylt = false,
                    måVurderesManuelt = true
                ),
                paragraf_11_6 = FrontendParagraf_11_6(
                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                    erOppfylt = false,
                    måVurderesManuelt = true
                ),
                paragraf_11_12 = FrontendParagraf_11_12(
                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                    erOppfylt = false,
                    måVurderesManuelt = true
                ),
                paragraf_11_29 = FrontendParagraf_11_29(
                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                    erOppfylt = false,
                    måVurderesManuelt = true
                ),
                vedtak = null
            )
        )

        søkerDao.insert(frontendSøker)

        assertEquals(1, rowCount("soker"))

        val frontendsøkere = søkerDao.select(listOf("12345678910"))

        assertEquals(frontendSøker, frontendsøkere.single())
    }

    @Test
    fun `Sletter søker fra database`() {
        søkerDao.insert(
            FrontendSøker(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                skjermet = false,
                sak = FrontendSak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null,
                    type = "11-5",
                    paragraf_11_2 = null,
                    paragraf_11_3 = null,
                    paragraf_11_4 = null,
                    paragraf_11_5 = null,
                    paragraf_11_6 = null,
                    paragraf_11_12 = null,
                    paragraf_11_29 = null
                )
            )
        )

        assertEquals(1, rowCount("soker"))

        søkerDao.delete("12345678910")

        assertEquals(0, rowCount("soker"))
    }

    @Test
    fun `Sletter ikke annen søker fra database`() {
        søkerDao.insert(
            FrontendSøker(
                personident = "01987654321",
                fødselsdato = LocalDate.of(1990, 1, 1),
                skjermet = false,
                sak = FrontendSak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null,
                    type = "11-5",
                    paragraf_11_2 = null,
                    paragraf_11_3 = null,
                    paragraf_11_4 = null,
                    paragraf_11_5 = null,
                    paragraf_11_6 = null,
                    paragraf_11_12 = null,
                    paragraf_11_29 = null
                )
            )
        )

        assertEquals(1, rowCount("soker"))

        søkerDao.insert(
            FrontendSøker(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                skjermet = false,
                sak = FrontendSak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null,
                    type = "11-5",
                    paragraf_11_2 = null,
                    paragraf_11_3 = null,
                    paragraf_11_4 = null,
                    paragraf_11_5 = null,
                    paragraf_11_6 = null,
                    paragraf_11_12 = null,
                    paragraf_11_29 = null
                )
            )
        )

        assertEquals(2, rowCount("soker"))

        søkerDao.delete("01987654321")

        assertEquals(1, rowCount("soker"))
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
