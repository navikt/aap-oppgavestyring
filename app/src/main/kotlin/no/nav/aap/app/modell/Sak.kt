package no.nav.aap.app.modell

import no.nav.aap.app.DtoSak
import no.nav.aap.app.db.DBSak
import no.nav.aap.app.modell.Vilkårsvurdering.Companion.oppgaveliste
import no.nav.aap.app.modell.Vilkårsvurdering.Companion.opprettVilkårsvurderinger
import java.util.*

internal class Sak(
    private val saksid: UUID,
    private val vilkårsvurdering: List<Vilkårsvurdering>
) {
    internal companion object {
        internal fun Iterable<Sak>.saksliste(
            personident: Personident,
            diskresjonskode: Diskresjonskode,
            skjermet: Skjermet,
            lokalkontorEnhetsnummer: LokalkontorEnhetsnummer
        ) = map {
            DBSak(
                personident = personident.toDBString(),
                saksid = it.saksid,
                diskresjonskode = diskresjonskode.toDBString(),
                skjermet = skjermet.toDBString(),
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