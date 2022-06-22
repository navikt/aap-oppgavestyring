package no.nav.aap.app.dao

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.RoleName
import no.nav.aap.app.axsys.InnloggetBruker
import no.nav.aap.app.dao.InitTestDatabase.dataSource
import no.nav.aap.app.frontendView.FrontendPersonopplysninger
import no.nav.aap.app.frontendView.Utfall
import no.nav.aap.app.kafka.SøkereKafkaDto
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
                SøkereKafkaDto.Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        SøkereKafkaDto.Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.OPPFYLT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            ),
            version = SøkereKafkaDto.VERSION
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
            brukernavn = "test@test.com",
            roller = listOf(RoleName.SAKSBEHANDLER),
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
                    SøkereKafkaDto.Sak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        tilstand = "",
                        sakstyper = listOf(),
                        vurderingsdato = LocalDate.now(),
                        vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                            tilstand = "",
                            løsningVurderingAvBeregningsdato = null
                        ),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        vedtak = null
                    )
                ),
                version = SøkereKafkaDto.VERSION
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
                    SøkereKafkaDto.Sak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        tilstand = "",
                        sakstyper = listOf(),
                        vurderingsdato = LocalDate.now(),
                        vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                            tilstand = "",
                            løsningVurderingAvBeregningsdato = null
                        ),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        vedtak = null
                    )
                ),
                version = SøkereKafkaDto.VERSION
            )
        )

        assertEquals(1, rowCount("soker"))

        søkerDao.insert(
            SøkereKafkaDto(
                personident = "12345678910",
                fødselsdato = LocalDate.of(1990, 1, 1),
                saker = listOf(
                    SøkereKafkaDto.Sak(
                        saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                        tilstand = "",
                        sakstyper = listOf(),
                        vurderingsdato = LocalDate.now(),
                        vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                            tilstand = "",
                            løsningVurderingAvBeregningsdato = null
                        ),
                        søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                        vedtak = null
                    )
                ),
                version = SøkereKafkaDto.VERSION
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
                SøkereKafkaDto.Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        SøkereKafkaDto.Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.OPPFYLT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            ),
            version = SøkereKafkaDto.VERSION
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
            brukernavn = "test@test.com",
            roller = listOf(RoleName.SAKSBEHANDLER),
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
                SøkereKafkaDto.Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        SøkereKafkaDto.Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.OPPFYLT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            ),
            version = SøkereKafkaDto.VERSION
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
            brukernavn = "test@test.com",
            roller = listOf(RoleName.SAKSBEHANDLER),
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
                SøkereKafkaDto.Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        SøkereKafkaDto.Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.OPPFYLT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            ),
            version = SøkereKafkaDto.VERSION
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
            brukernavn = "test@test.com",
            roller = listOf(RoleName.SAKSBEHANDLER),
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
                SøkereKafkaDto.Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        SøkereKafkaDto.Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.OPPFYLT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            ),
            version = SøkereKafkaDto.VERSION
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
            brukernavn = "test@test.com",
            roller = listOf(RoleName.SAKSBEHANDLER),
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
                SøkereKafkaDto.Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        SøkereKafkaDto.Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.OPPFYLT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            ),
            version = SøkereKafkaDto.VERSION
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
            brukernavn = "test@test.com",
            roller = listOf(RoleName.SAKSBEHANDLER),
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
                SøkereKafkaDto.Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        SøkereKafkaDto.Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.OPPFYLT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            ),
            version = SøkereKafkaDto.VERSION
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
            brukernavn = "test@test.com",
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
                SøkereKafkaDto.Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        SøkereKafkaDto.Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.OPPFYLT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            ),
            version = SøkereKafkaDto.VERSION
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
            brukernavn = "test@test.com",
            roller = listOf(RoleName.VEILEDER),
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
                SøkereKafkaDto.Sak(
                    saksid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417367"),
                    tilstand = "",
                    sakstyper = listOf(
                        SøkereKafkaDto.Sakstype(
                            type = "11-5",
                            aktiv = true,
                            vilkårsvurderinger = listOf(
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417360"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417361"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417362"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_4",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.OPPFYLT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417363"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417364"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_6",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417365"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_12",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                ),
                                SøkereKafkaDto.Vilkårsvurdering(
                                    vilkårsvurderingsid = UUID.fromString("f422222c-8606-4426-b929-c2b8b4417366"),
                                    vurdertAv = null,
                                    godkjentAv = null,
                                    paragraf = "PARAGRAF_11_29",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "",
                                    utfall = Utfall.IKKE_VURDERT
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = SøkereKafkaDto.VurderingAvBeregningsdato(
                        tilstand = "",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    søknadstidspunkt = LocalDate.of(2022, 1, 1).atStartOfDay(),
                    vedtak = null
                )
            ),
            version = SøkereKafkaDto.VERSION
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
            brukernavn = "test@test.com",
            roller = listOf(RoleName.VEILEDER),
            tilknyttedeEnheter = listOf("4321"),
            harSkjermingsrolle = false,
        )

        val frontendsøkere = søkerDao.select(innloggetBruker)

        assertEquals(0, frontendsøkere.size)
    }

    @Test
    fun `Henter bare den versjonen av søker som støttes`() {
        val søkerGammelVersjon = SøkereKafkaDto(
            personident = "12345678910",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = emptyList(),
            version = SøkereKafkaDto.VERSION - 1
        )
        val søkerNyVersjon = SøkereKafkaDto(
            personident = "10987654321",
            fødselsdato = LocalDate.of(1990, 1, 1),
            saker = emptyList(),
            version = SøkereKafkaDto.VERSION
        )

        søkerDao.insert(søkerGammelVersjon)
        søkerDao.insert(søkerNyVersjon)

        val personopplysningerGammelSøker = FrontendPersonopplysninger(
            personident = "12345678910",
            norgEnhetId = "1234",
            adressebeskyttelse = "UGRADERT",
            geografiskTilknytning = "0001",
            erSkjermet = false,
            erSkjermetFom = null,
            erSkjermetTom = null
        )

        val personopplysningerNySøker = FrontendPersonopplysninger(
            personident = "10987654321",
            norgEnhetId = "1234",
            adressebeskyttelse = "UGRADERT",
            geografiskTilknytning = "0001",
            erSkjermet = false,
            erSkjermetFom = null,
            erSkjermetTom = null
        )

        personopplysningerDao.insert(personopplysningerGammelSøker)
        personopplysningerDao.insert(personopplysningerNySøker)

        val innloggetBruker = InnloggetBruker(
            brukernavn = "test@test.com",
            roller = listOf(RoleName.SAKSBEHANDLER),
            tilknyttedeEnheter = listOf("1234"),
            harSkjermingsrolle = false,
        )

        val søkere = søkerDao.select(innloggetBruker)

        assertEquals(2, rowCount("soker"))
        assertEquals(1, søkere.size)
        assertEquals(SøkereKafkaDto.VERSION, søkere.single().version)
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
