package no.nav.aap

import no.nav.aap.app.Repository
import no.nav.aap.app.axsys.InnloggetBruker
import no.nav.aap.app.frontendView.FrontendMottaker
import no.nav.aap.app.frontendView.FrontendPersonopplysninger
import no.nav.aap.app.frontendView.FrontendSøker
import no.nav.aap.app.kafka.SøkereKafkaDto
import no.nav.aap.app.modell.Rolle
import no.nav.aap.app.topology
import no.nav.aap.kafka.streams.uml.KStreamsUML
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.*

internal class GenerateUML {

    @Test
    fun `generate topology UML`() {
        val topology = topology(repo)
        KStreamsUML.create(topology).also { log.info("Generated topology UML ${it.absoluteFile}") }
    }

    private companion object {
        private val log = LoggerFactory.getLogger("GenerateUML")

        private val repo = object : Repository {
            override fun hentSøker(personident: String, innloggetBruker: InnloggetBruker) = error("Ikke implementert")
            override fun lagreSøker(søker: SøkereKafkaDto) = error("Ikke implementert")
            override fun hentSøkere(innloggetBruker: InnloggetBruker) = error("Ikke implementert")
            override fun slettSøker(personident: String) = error("Ikke implementert")
            override fun lagrePersonopplysninger(fp: FrontendPersonopplysninger) = error("Ikke implementert")
            override fun hentPersonopplysninger(personident: String) = error("Ikke implementert")
            override fun lagreMottaker(frontendMottaker: FrontendMottaker) = error("Ikke implementert")
            override fun tildelSak(saksid: UUID, ident: String, rolle: Rolle) = error("Ikke implementert")
            override fun endreTildeling(saksid: UUID, ident: String, nyIdent: String, nyRolle: Rolle) = error("Ikke implementert")
            override fun fjernTildeling(saksid: UUID, ident: String) = error("Ikke implementert")
        }
    }
}
