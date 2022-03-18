package no.nav.aap.app.modell

import no.nav.aap.app.DtoSøker
import no.nav.aap.app.modell.Sak.Companion.saksliste

class Søker internal constructor(
    private val personident: Personident,
    private val geografiskTilknytning: GeografiskTilknytning,
    private val diskresjonskode: Diskresjonskode,
    private val skjermet: Skjermet,
    private val lokalkontorEnhetsnummer: LokalkontorEnhetsnummer,
    private val saker: List<Sak>
) {

    fun saksliste() = saker.saksliste(
        personident,
        diskresjonskode,
        skjermet,
        lokalkontorEnhetsnummer
    )

    companion object {
        fun opprettSøker(dtoSøker: DtoSøker) = Søker(
            personident = Personident(dtoSøker.personident),
            geografiskTilknytning = GeografiskTilknytning(dtoSøker.geografiskTilknytning),
            diskresjonskode = enumValueOf(dtoSøker.diskresjonskode),
            skjermet = Skjermet(dtoSøker.skjermet),
            lokalkontorEnhetsnummer = LokalkontorEnhetsnummer(dtoSøker.lokalkontorEnhetsnummer),
            saker = dtoSøker.saker.map { Sak.opprettSak(it) }
        )
    }
}

