package no.nav.aap.app.dao

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.dao.InitTestDatabase.dataSource
import no.nav.aap.app.frontendView.*
import no.nav.aap.app.axsys.InnloggetBruker
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

internal class SøkerDaoTest : DatabaseTestBase() {
    private val søkerDao = SøkerDao(dataSource)
    private val personopplysningerDao = PersonopplysningerDao(dataSource)

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
                    måVurderesManuelt = true,
                    kravOmNedsattArbeidsevneErOppfylt = null,
                    nedsettelseSkyldesSykdomEllerSkade = null
                ),
                paragraf_11_6 = FrontendParagraf_11_6(
                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                    erOppfylt = false,
                    måVurderesManuelt = true,
                    harBehovForBehandling = null,
                    harBehovForTiltak = null,
                    harMulighetForÅKommeIArbeid = null
                ),
                paragraf_11_12 = FrontendParagraf_11_12(
                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                    erOppfylt = false,
                    måVurderesManuelt = true,
                    bestemmesAv = null,
                    unntak = null,
                    unntaksbegrunnelse = null,
                    manueltSattVirkningsdato = null
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

        val personopplysninger = FrontendPersonopplysninger(
            personident = "12345678910",
            norgEnhetId = "1234",
            adressebeskyttelse = "UGRADERT",
            geografiskTilknytning = "0001",
            erSkjermet = false,
            erSkjermetFom = null,
            erSkjermetTom = null
        )

        personopplysningerDao.insert(personopplysninger)

        assertEquals(1, rowCount("soker"))

        val innloggetBruker = InnloggetBruker(
            ident = "Z000001",
            roller = listOf("UGRADERT"),
            tilknyttetEnhet = listOf("1234")
        )

        val frontendsøkere = søkerDao.select(listOf("12345678910"), innloggetBruker)

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

    @Test
    fun `Bruker har fortrolig adresse, innlogget saksbehandler har ikke rett til å se denne`() {
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
                    måVurderesManuelt = true,
                    kravOmNedsattArbeidsevneErOppfylt = null,
                    nedsettelseSkyldesSykdomEllerSkade = null
                ),
                paragraf_11_6 = FrontendParagraf_11_6(
                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                    erOppfylt = false,
                    måVurderesManuelt = true,
                    harBehovForBehandling = null,
                    harBehovForTiltak = null,
                    harMulighetForÅKommeIArbeid = null
                ),
                paragraf_11_12 = FrontendParagraf_11_12(
                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                    erOppfylt = false,
                    måVurderesManuelt = true,
                    bestemmesAv = null,
                    unntak = null,
                    unntaksbegrunnelse = null,
                    manueltSattVirkningsdato = null
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

        val personopplysninger = FrontendPersonopplysninger(
            personident = "12345678910",
            norgEnhetId = "1234",
            adressebeskyttelse = "FORTROLIG",
            geografiskTilknytning = "0001",
            erSkjermet = false,
            erSkjermetFom = null,
            erSkjermetTom = null
        )

        personopplysningerDao.insert(personopplysninger)

        val innloggetBruker = InnloggetBruker(
            ident = "Z000001",
            roller = listOf(""),
            tilknyttetEnhet = listOf("1234")
        )

        val frontendsøkere = søkerDao.select(innloggetBruker)

        assertEquals(0, frontendsøkere.size)
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
