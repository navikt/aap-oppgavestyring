package no.nav.aap.app.dao

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.db.DBOppgave
import no.nav.aap.app.db.DBSak
import no.nav.aap.app.frontendView.FrontendSak
import no.nav.aap.app.frontendView.FrontendSakstype
import no.nav.aap.app.frontendView.FrontendSøker
import no.nav.aap.app.frontendView.FrontendVilkårsvurdering
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
            saker = listOf(
            FrontendSak(
                fødselsdato = LocalDate.of(1990, 1, 1),
                tilstand = "SØKNAD_MOTTATT",
                sakstype = FrontendSakstype(
                    type = "STANDARD",
                    vilkårsvurderinger = listOf(
                        FrontendVilkårsvurdering(
                            paragraf = "PARAGRAF_11_2",
                            ledd = listOf("LEDD_1", "LEDD_2"),
                            tilstand = "SØKNAD_MOTTATT",
                            måVurderesManuelt = true
                        ),
                        FrontendVilkårsvurdering(
                            paragraf = "PARAGRAF_11_3",
                            ledd = listOf("LEDD_1", "LEDD_2", "LEDD_3"),
                            tilstand = "SØKNAD_MOTTATT",
                            måVurderesManuelt = true
                        ),
                        FrontendVilkårsvurdering(
                            paragraf = "PARAGRAF_11_4",
                            ledd = listOf("LEDD_1"),
                            tilstand = "OPPFYLT",
                            måVurderesManuelt = false
                        ),
                        FrontendVilkårsvurdering(
                            paragraf = "PARAGRAF_11_4",
                            ledd = listOf("LEDD_2", "LEDD_3"),
                            tilstand = "IKKE_RELEVANT",
                            måVurderesManuelt = false
                        ),
                        FrontendVilkårsvurdering(
                            paragraf = "PARAGRAF_11_5",
                            ledd = listOf("LEDD_1", "LEDD_2"),
                            tilstand = "SØKNAD_MOTTATT",
                            måVurderesManuelt = true
                        ),
                        FrontendVilkårsvurdering(
                            paragraf = "PARAGRAF_11_6",
                            ledd = listOf("LEDD_1"),
                            tilstand = "SØKNAD_MOTTATT",
                            måVurderesManuelt = true
                        ),
                        FrontendVilkårsvurdering(
                            paragraf = "PARAGRAF_11_12",
                            ledd = listOf("LEDD_1"),
                            tilstand = "SØKNAD_MOTTATT",
                            måVurderesManuelt = true
                        ),
                        FrontendVilkårsvurdering(
                            paragraf = "PARAGRAF_11_29",
                            ledd = listOf("LEDD_1"),
                            tilstand = "SØKNAD_MOTTATT",
                            måVurderesManuelt = true
                        )
                    )
                ),
                vedtak = null
            )
        ))

        søkerDao.insert(frontendSøker)

        assertEquals(1, rowCount("soker"))

        val frontendsøkere = søkerDao.select(listOf("12345678910"))

        assertEquals(frontendSøker, frontendsøkere.single())
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
