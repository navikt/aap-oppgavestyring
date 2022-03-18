package no.nav.aap.app.modell

import no.nav.aap.app.*
import no.nav.aap.app.db.DBOppgave
import no.nav.aap.app.db.DBSak
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals

internal class SøkerTest {

    @Test
    fun `Dette er første søker - hvordan bli den behandlet`() {
        val dtoSøker = DtoSøker(
            personident = "12345678910",
            geografiskTilknytning = "030102",
            diskresjonskode = "UGRADERT",
            egenAnsatt = false,
            lokalkontorEnhetsnummer = "0315",
            saker = listOf(
                DtoSak(
                    saksid = UUID.fromString("a088e6bc-8b74-4b9f-a8b7-c33738859f09"),
                    tilstand = "SØKNAD_MOTTATT",
                    sakstyper = listOf(
                        DtoSakstype(
                            type = "STANDARD",
                            vilkårsvurderinger = listOf(
                                DtoVilkårsvurdering(
                                    vilkårsvurderingid = UUID.fromString("a088e6bc-8b74-4b9f-a8b7-c33738859f00"),
                                    paragraf = "PARAGRAF_11_2",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = false
                                ),
                                DtoVilkårsvurdering(
                                    vilkårsvurderingid = UUID.fromString("a088e6bc-8b74-4b9f-a8b7-c33738859f01"),
                                    paragraf = "PARAGRAF_11_3",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                ),
                                DtoVilkårsvurdering(
                                    vilkårsvurderingid = UUID.fromString("a088e6bc-8b74-4b9f-a8b7-c33738859f02"),
                                    paragraf = "PARAGRAF_11_5",
                                    ledd = listOf("LEDD_1"),
                                    tilstand = "SØKNAD_MOTTATT",
                                    måVurderesManuelt = true
                                )
                            )
                        )
                    ),
                    vurderingsdato = LocalDate.now(),
                    vurderingAvBeregningsdato = DtoVurderingAvBeregningsdato(
                        tilstand = "SØKNAD_MOTTATT",
                        løsningVurderingAvBeregningsdato = null
                    ),
                    vedtak = null
                )
            )
        )

        val søker = Søker.opprettSøker(dtoSøker)

        val saklistemedoppgaver = listOf(
            DBSak(
                personident = "12345678910",
                saksid = UUID.fromString("a088e6bc-8b74-4b9f-a8b7-c33738859f09"),
                /*
                Geografisk tilknytning og diskresjonskode er bakt inn i lokalkontorEnhetsnummer for lokalkontor.
                For NAY må diskresjonskode håndteres i tillegg */
                diskresjonskode = "UGRADERT",
                egenAnsatt = false,
                lokalkontorEnhetsnummer = "0315",
                oppgaver = listOf(
                    DBOppgave(
                        oppgaveid = UUID.fromString("a088e6bc-8b74-4b9f-a8b7-c33738859f00"),
                        status = "BEHANDLET",
                        nayEllerKontor = "NAY",
                        roller = listOf("BEHANDLER")
                    ),
                    DBOppgave(
                        oppgaveid = UUID.fromString("a088e6bc-8b74-4b9f-a8b7-c33738859f01"),
                        status = "IKKE_VURDERT",
                        nayEllerKontor = "NAY",
                        roller = listOf("BEHANDLER")
                    ),
                    DBOppgave(
                        oppgaveid = UUID.fromString("a088e6bc-8b74-4b9f-a8b7-c33738859f02"),
                        status = "IKKE_VURDERT",
                        nayEllerKontor = "KONTOR",
                        roller = listOf("BEHANDLER")
                    )
                )
            )
        )

        assertEquals(saklistemedoppgaver, søker.saksliste())
    }
}