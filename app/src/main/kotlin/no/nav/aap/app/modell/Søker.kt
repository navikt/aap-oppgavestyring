package no.nav.aap.app.modell

import no.nav.aap.app.DtoSak
import no.nav.aap.app.DtoSøker
import no.nav.aap.app.DtoVilkårsvurdering
import no.nav.aap.app.db.DBOppgave
import no.nav.aap.app.db.DBSak
import no.nav.aap.app.modell.Sak.Companion.saksliste
import no.nav.aap.app.modell.Vilkårsvurdering.Companion.oppgaveliste
import no.nav.aap.app.modell.Vilkårsvurdering.Companion.opprettVilkårsvurderinger
import java.util.*

class Søker internal constructor(
    private val personident: Personident,
    private val geografiskTilknytning: GeografiskTilknytning,
    private val diskresjonskode: Diskresjonskode,
    private val egenAnsatt: EgenAnsatt,
    private val lokalkontorEnhetsnummer: LokalkontorEnhetsnummer,
    private val saker: List<Sak>
) {

    fun saksliste() = saker.saksliste(
        personident,
        diskresjonskode,
        egenAnsatt,
        lokalkontorEnhetsnummer
    )

    companion object {
        fun opprettSøker(dtoSøker: DtoSøker) = Søker(
            personident = Personident(dtoSøker.personident),
            geografiskTilknytning = GeografiskTilknytning(dtoSøker.geografiskTilknytning),
            diskresjonskode = enumValueOf(dtoSøker.diskresjonskode),
            egenAnsatt = EgenAnsatt(dtoSøker.egenAnsatt),
            lokalkontorEnhetsnummer = LokalkontorEnhetsnummer(dtoSøker.lokalkontorEnhetsnummer),
            saker = dtoSøker.saker.map { Sak.opprettSak(it) }
        )
    }
}

internal class Personident(private val ident: String) {
    internal fun toDBString() = ident
}

internal class GeografiskTilknytning(private val geografiskTilknytning: String)

internal enum class Diskresjonskode {
    UGRADERT,
    FORTROLIG,
    STRENGT_FORTROLIG,
    STRENGT_FORTROLIG_UTLAND;

    internal fun toDBString() = name
}

internal class EgenAnsatt(private val erEgenAnsatt: Boolean) {
    internal fun toDBString() = erEgenAnsatt
}

internal class LokalkontorEnhetsnummer(private val lokalkontorEnhetsnummer: String) {
    internal fun toDBString() = lokalkontorEnhetsnummer
}

internal class Sak(
    private val saksid: UUID,
    private val vilkårsvurdering: List<Vilkårsvurdering>
) {
    internal companion object {
        internal fun Iterable<Sak>.saksliste(
            personident: Personident,
            diskresjonskode: Diskresjonskode,
            egenAnsatt: EgenAnsatt,
            lokalkontorEnhetsnummer: LokalkontorEnhetsnummer
        ) = map {
            DBSak(
                personident = personident.toDBString(),
                saksid = it.saksid,
                diskresjonskode = diskresjonskode.toDBString(),
                egenAnsatt = egenAnsatt.toDBString(),
                lokalkontorEnhetsnummer = lokalkontorEnhetsnummer.toDBString(),
                oppgaver = it.vilkårsvurdering.oppgaveliste()
            )
        }

        internal fun opprettSak(dtoSak: DtoSak) = Sak(
            saksid = dtoSak.saksid,
            vilkårsvurdering = dtoSak.sakstyper.last().vilkårsvurderinger.opprettVilkårsvurderinger()
        )
    }
}

internal class Vilkårsvurdering(
    private val vilkårsvurderingid: UUID,
    private val måVurderesManuelt: Boolean,
    private val paragraf: Paragraf,
    private val ledd: List<Ledd>
) {
    internal companion object {
        internal fun Iterable<Vilkårsvurdering>.oppgaveliste() = map {
            DBOppgave(
                oppgaveid = it.vilkårsvurderingid,
                status = if (it.måVurderesManuelt) "IKKE_VURDERT" else "BEHANDLET",
                nayEllerKontor = it.paragraf.skalLøsesAv.name,
                roller = listOf(Rolle.BEHANDLER.name)
            )
        }

        internal fun Iterable<DtoVilkårsvurdering>.opprettVilkårsvurderinger() = this
            .map {
                Vilkårsvurdering(
                    vilkårsvurderingid = it.vilkårsvurderingid,
                    måVurderesManuelt = it.måVurderesManuelt,
                    paragraf = enumValueOf(it.paragraf),
                    ledd = it.ledd.map(::enumValueOf)
                )
            }
    }
}

internal enum class Paragraf(internal val skalLøsesAv: Enhet) {
    PARAGRAF_11_2(Enhet.NAY),
    PARAGRAF_11_3(Enhet.NAY),
    PARAGRAF_11_4(Enhet.NAY),
    PARAGRAF_11_5(Enhet.KONTOR),
    PARAGRAF_11_6(Enhet.NAY),
    PARAGRAF_11_9(Enhet.KONTOR),
    PARAGRAF_11_10(Enhet.KONTOR),
    PARAGRAF_11_11(Enhet.KONTOR),
    PARAGRAF_11_12(Enhet.NAY),
    PARAGRAF_11_14(Enhet.NAY),
    PARAGRAF_11_29(Enhet.NAY)
}

internal enum class Ledd {
    LEDD_1,
    LEDD_2,
    LEDD_3;

    operator fun plus(other: Ledd) = listOf(this, other)
}

//Hvilken linje har ansvar for oppgaven
enum class Enhet {
    NAY,
    KONTOR
}

//Hvilke(n) rolle må veileder/saksbehandler ha
enum class Rolle {
    BEHANDLER, FATTER, EGEN_ANSATT, DISKRESJONSKODEBEHANDLER //⁉️
}

//Hva går oppgaven ut på
data class Hva(
    private val paragraf: String,
    private val ledd: List<String>
)
