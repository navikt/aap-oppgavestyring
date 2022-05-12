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
internal class MottakerDaoTest {

    private val postgres = PostgreSQLContainer<Nothing>("postgres:14")
    private lateinit var dataSource: DataSource
    private lateinit var flyway: Flyway

    private lateinit var mottakerDao: MottakerDao

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

        mottakerDao = MottakerDao(dataSource)
    }

    @BeforeEach
    internal fun beforeEach() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun `Lagrer frontendmottaker i database`() {
        val frontendMottaker = FrontendMottaker(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            vedtakshistorikk = listOf(
                FrontendMottakerVedtak(
                    vedtaksid = UUID.randomUUID(),
                    innvilget = true,
                    grunnlagsfaktor = 4.0,
                    vedtaksdato = LocalDate.of(2022, 5, 2),
                    virkningsdato = LocalDate.of(2022, 5, 2),
                    fødselsdato = LocalDate.of(1990, 1, 1)
                )
            ),
            aktivitetstidslinje = listOf(
                FrontendMeldeperiode(
                    dager = listOf(
                        FrontendDag(
                            dato = LocalDate.of(2022, 5, 2),
                            arbeidstimer = 0.0,
                            type = "ARBEIDSDAG"
                        )
                    )
                )
            ),
            utbetalingstidslinjehistorikk = listOf(
                FrontendUtbetalingstidslinje(
                    dager = listOf(
                        FrontendUtbetalingstidslinjedag(
                            dato = LocalDate.of(2022, 5, 2),
                            grunnlagsfaktor = 4.0,
                            barnetillegg = 0.0,
                            grunnlag = 425596.0,
                            dagsats = 1080.0,
                            høyestebeløpMedBarnetillegg = 1473.0,
                            beløpMedBarnetillegg = 1080.0,
                            beløp = 1080.0,
                            arbeidsprosent = 0.0,
                        )
                    )
                )
            ),
            oppdragshistorikk = listOf(
                FrontendOppdrag(
                    mottaker = "12345678910",
                    fagområde = "Arbeidsavklaringspenger",
                    linjer = listOf(
                        FrontendUtbetalingslinje(
                            fom = LocalDate.of(2022, 5, 2),
                            tom = LocalDate.of(2022, 5, 2),
                            satstype = "DAG",
                            beløp = 1080,
                            aktuellDagsinntekt = 1080,
                            grad = 100,
                            refFagsystemId = null,
                            delytelseId = 1,
                            refDelytelseId = null,
                            endringskode = "NY",
                            klassekode = "RefusjonIkkeOpplysningspliktig",
                            datoStatusFom = null
                        )
                    ),
                    fagsystemId = "NQGM2S4XEJEJ3AYTU7NJDMDNO4",
                    endringskode = "NY",
                    nettoBeløp = 1080,
                    overføringstidspunkt = LocalDate.of(2022, 5, 2).atStartOfDay(),
                    avstemmingsnøkkel = null,
                    status = null,
                    tidsstempel = LocalDate.of(2022, 5, 2).atStartOfDay()
                )
            )
        )

        mottakerDao.insert(frontendMottaker)

        assertEquals(1, rowCount("mottaker"))

        val frontendsøkere = mottakerDao.select(listOf("12345678910"))

        assertEquals(frontendMottaker, frontendsøkere.single())
    }

    @Test
    fun `Sletter mottaker fra database`() {
        mottakerDao.insert(
            FrontendMottaker(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                vedtakshistorikk = emptyList(),
                aktivitetstidslinje = emptyList(),
                utbetalingstidslinjehistorikk = emptyList(),
                oppdragshistorikk = emptyList()
            )
        )

        assertEquals(1, rowCount("mottaker"))

        mottakerDao.delete("12345678910")

        assertEquals(0, rowCount("mottaker"))
    }

    @Test
    fun `Sletter ikke annen mottaker fra database`() {
        mottakerDao.insert(
            FrontendMottaker(
                personident = "01987654321",
                fødselsdato = LocalDate.of(1990, 1, 1),
                vedtakshistorikk = emptyList(),
                aktivitetstidslinje = emptyList(),
                utbetalingstidslinjehistorikk = emptyList(),
                oppdragshistorikk = emptyList()
            )
        )

        assertEquals(1, rowCount("mottaker"))

        mottakerDao.insert(
            FrontendMottaker(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                vedtakshistorikk = emptyList(),
                aktivitetstidslinje = emptyList(),
                utbetalingstidslinjehistorikk = emptyList(),
                oppdragshistorikk = emptyList()
            )
        )

        assertEquals(2, rowCount("mottaker"))

        mottakerDao.delete("01987654321")

        assertEquals(1, rowCount("mottaker"))
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
