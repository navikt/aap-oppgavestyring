package no.nav.aap.app.modell

import no.nav.aap.app.DtoVilkårsvurdering
import no.nav.aap.app.db.DBOppgave
import java.util.*

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