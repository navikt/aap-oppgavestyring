package no.nav.aap.app.dao

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.dao.InitTestDatabase.dataSource
import no.nav.aap.app.frontendView.FrontendPersonopplysninger
import no.nav.aap.app.frontendView.FrontendSak
import no.nav.aap.app.frontendView.FrontendSøker
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

internal class PersonopplysningerDaoTest : DatabaseTestBase() {
    private val personopplysningerDao = PersonopplysningerDao(dataSource)
    private val søkerDao = SøkerDao(dataSource)

    @Test
    fun `Lagrer personopplysninger i database`() {
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

    @Test
    fun `Sletter personopplysninger fra database`() {
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
        personopplysningerDao.insert(
            FrontendPersonopplysninger(
                personident = "12345678910",
                norgEnhetId = "4201",
                adressebeskyttelse = "UGRADERT",
                geografiskTilknytning = "030101", // bydel
                erSkjermet = false,
                erSkjermetFom = null,
                erSkjermetTom = null,
            )
        )

        assertEquals(1, rowCount("personopplysninger"))

        personopplysningerDao.delete("12345678910")

        assertEquals(0, rowCount("personopplysninger"))
    }

    @Test
    fun `Sletter ikke andre personopplysninger fra database`() {
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
        personopplysningerDao.insert(
            FrontendPersonopplysninger(
                personident = "01987654321",
                norgEnhetId = "4201",
                adressebeskyttelse = "UGRADERT",
                geografiskTilknytning = "030101", // bydel
                erSkjermet = false,
                erSkjermetFom = null,
                erSkjermetTom = null,
            )
        )

        assertEquals(1, rowCount("personopplysninger"))

        personopplysningerDao.insert(
            FrontendPersonopplysninger(
                personident = "12345678910",
                norgEnhetId = "4201",
                adressebeskyttelse = "UGRADERT",
                geografiskTilknytning = "030101", // bydel
                erSkjermet = false,
                erSkjermetFom = null,
                erSkjermetTom = null,
            )
        )

        assertEquals(2, rowCount("personopplysninger"))

        personopplysningerDao.delete("01987654321")

        assertEquals(1, rowCount("personopplysninger"))
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
