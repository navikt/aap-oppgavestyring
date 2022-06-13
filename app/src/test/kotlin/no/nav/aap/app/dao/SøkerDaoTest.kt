package no.nav.aap.app.dao

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.axsys.InnloggetBruker
import no.nav.aap.app.dao.InitTestDatabase.dataSource
import no.nav.aap.app.frontendView.*
import no.nav.aap.app.kafka.*
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
        val frontendSøker = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = listOf(
                Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
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
            roller = listOf("SAKSBEHANDLER"),
            tilknyttedeEnheter = listOf("1234"),
        )

        val frontendsøkere = søkerDao.select(listOf("12345678910"), innloggetBruker)

        assertEquals(frontendSøker, frontendsøkere.single())
    }

    @Test
    fun `Sletter søker fra database`() {
        søkerDao.insert(
            SøkereKafkaDto(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                saker = listOf(
                    Sak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        tilstand = "",
                        sakstyper = listOf(),
                        vurderingsdato = LocalDate.now(),
                        vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                            tilstand = "",
                            løsningVurderingAvBeregningsdato = null
                        ),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        vedtak = null
                    )
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
            SøkereKafkaDto(
                personident = "01987654321",
                fødselsdato = LocalDate.of(1990, 1, 1),
                saker = listOf(
                    Sak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        tilstand = "",
                        sakstyper = listOf(),
                        vurderingsdato = LocalDate.now(),
                        vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                            tilstand = "",
                            løsningVurderingAvBeregningsdato = null
                        ),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        vedtak = null
                    )
                )
            )
        )

        assertEquals(1, rowCount("soker"))

        søkerDao.insert(
            SøkereKafkaDto(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                saker = listOf(
                    Sak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        tilstand = "",
                        sakstyper = listOf(),
                        vurderingsdato = LocalDate.now(),
                        vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                            tilstand = "",
                            løsningVurderingAvBeregningsdato = null
                        ),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        vedtak = null
                    )
                )
            )
        )

        assertEquals(2, rowCount("soker"))

        søkerDao.delete("01987654321")

        assertEquals(1, rowCount("soker"))
    }

    @Test
    fun `Bruker har fortrolig adresse, innlogget saksbehandler har ikke rett til å se denne`() {
        val frontendSøker = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = listOf(
                Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
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
            roller = listOf("SAKSBEHANDLER"),
            tilknyttedeEnheter = listOf("1234"),
        )

        val frontendsøkere = søkerDao.select(innloggetBruker)

        assertEquals(0, frontendsøkere.size)
    }

    @Test
    fun `Bruker har skjerming fom og ingen tom, innlogget saksbehandler har ikke rett til å se denne`() {
        val frontendSøker = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = listOf(
                Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            )
        )

        søkerDao.insert(frontendSøker)

        val personopplysninger = FrontendPersonopplysninger(
            personident = "12345678910",
            norgEnhetId = "1234",
            adressebeskyttelse = "UGRADERT",
            geografiskTilknytning = "0001",
            erSkjermet = true,
            erSkjermetFom = LocalDate.now().minusWeeks(1),
            erSkjermetTom = null
        )

        personopplysningerDao.insert(personopplysninger)

        val innloggetBruker = InnloggetBruker(
            ident = "Z000001",
            roller = listOf("SAKSBEHANDLER"),
            tilknyttedeEnheter = listOf("1234"),
        )

        val frontendsøkere = søkerDao.select(innloggetBruker)

        assertEquals(0, frontendsøkere.size)
    }

    @Test
    fun `Bruker har ikke skjerming, fom og tom er begge passert, innlogget saksbehandler har rett til å se denne`() {
        val frontendSøker = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = listOf(
                Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            )
        )

        søkerDao.insert(frontendSøker)

        val personopplysninger = FrontendPersonopplysninger(
            personident = "12345678910",
            norgEnhetId = "1234",
            adressebeskyttelse = "UGRADERT",
            geografiskTilknytning = "0001",
            erSkjermet = false,
            erSkjermetFom = LocalDate.now().minusWeeks(1),
            erSkjermetTom = LocalDate.now().minusDays(1)
        )

        personopplysningerDao.insert(personopplysninger)

        val innloggetBruker = InnloggetBruker(
            ident = "Z000001",
            roller = listOf("SAKSBEHANDLER"),
            tilknyttedeEnheter = listOf("1234"),
        )

        val frontendsøkere = søkerDao.select(innloggetBruker)

        assertEquals(1, frontendsøkere.size)
    }

    @Test
    fun `Bruker har skjerming, innlogget saksbehandler har rollen, og derfor rett til å se denne`() {
        val frontendSøker = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = listOf(
                Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            )
        )

        søkerDao.insert(frontendSøker)

        val personopplysninger = FrontendPersonopplysninger(
            personident = "12345678910",
            norgEnhetId = "1234",
            adressebeskyttelse = "UGRADERT",
            geografiskTilknytning = "0001",
            erSkjermet = true,
            erSkjermetFom = LocalDate.now().minusWeeks(1),
            erSkjermetTom = null
        )

        personopplysningerDao.insert(personopplysninger)

        val innloggetBruker = InnloggetBruker(
            ident = "Z000001",
            roller = listOf("SAKSBEHANDLER"),
            tilknyttedeEnheter = listOf("1234"),
            harSkjermingsrolle = true,
        )

        val frontendsøkere = søkerDao.select(innloggetBruker)

        assertEquals(1, frontendsøkere.size)
    }

    @Test
    fun `Innlogget som saksbehandler - norg enhet har ingen påvirkning - har derfor rett til å se denne`() {
        val frontendSøker = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = listOf(
                Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
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

        val innloggetBruker = InnloggetBruker(
            ident = "Z000001",
            roller = listOf("SAKSBEHANDLER"),
            tilknyttedeEnheter = listOf("1234"),
            harSkjermingsrolle = false,
        )

        val frontendsøkere = søkerDao.select(innloggetBruker)

        assertEquals(1, frontendsøkere.size)
    }

    @Test
    fun `Innlogget uten saksbehandler eller veilederrolle - har ikke rett til å se denne`() {
        val frontendSøker = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = listOf(
                Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
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

        val innloggetBruker = InnloggetBruker(
            ident = "Z000001",
            roller = emptyList(),
            tilknyttedeEnheter = listOf("1234"),
            harSkjermingsrolle = false,
        )

        val frontendsøkere = søkerDao.select(innloggetBruker)

        assertEquals(0, frontendsøkere.size)
    }

    @Test
    fun `Innlogget som veileder - riktig norg enhet - har derfor rett til å se denne`() {
        val frontendSøker = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = listOf(
                Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
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

        val innloggetBruker = InnloggetBruker(
            ident = "Z000001",
            roller = listOf("VEILEDER"),
            tilknyttedeEnheter = listOf("1234"),
            harSkjermingsrolle = false,
        )

        val frontendsøkere = søkerDao.select(innloggetBruker)

        assertEquals(1, frontendsøkere.size)
    }

    @Test
    fun `Innlogget som veileder - annen norg enhet - har derfor ikke rett til å se denne`() {
        val frontendSøker = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = listOf(
                Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                ),
                                Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    måVurderesManuelt = true
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
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

        val innloggetBruker = InnloggetBruker(
            ident = "Z000001",
            roller = listOf("VEILEDER"),
            tilknyttedeEnheter = listOf("4321"),
            harSkjermingsrolle = false,
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
