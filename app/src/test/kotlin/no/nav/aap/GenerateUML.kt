package no.nav.aap

import no.nav.aap.app.Repository
import no.nav.aap.app.frontendView.FrontendMottaker
import no.nav.aap.app.frontendView.FrontendPersonopplysninger
import no.nav.aap.app.frontendView.FrontendSøker
import no.nav.aap.app.modell.Rolle
import no.nav.aap.app.topology
import no.nav.aap.kafka.streams.uml.KStreamsUML
import org.apache.kafka.streams.StreamsBuilder
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.*

internal class GenerateUML {

    @Test
    fun `generate topology UML`() {
        val topology = StreamsBuilder().topology(repo).build()
        KStreamsUML.create(topology).also { log.info("Generated topology UML ${it.absoluteFile}") }
    }

    private companion object {
        private val log = LoggerFactory.getLogger("GenerateUML")

        private val repo = object : Repository {
            override fun hentSøker(personident: String) = TODO("")
            override fun lagreSøker(frontendSøker: FrontendSøker) = TODO("")
            override fun hentSøkere() = TODO("")
            override fun slettSøker(personident: String) = TODO("")
            override fun lagrePersonopplysninger(fp: FrontendPersonopplysninger) = TODO("")
            override fun hentPersonopplysninger(personident: String) = TODO("")
            override fun lagreMottaker(frontendMottaker: FrontendMottaker) = TODO("")
            override fun tildelSak(saksid: UUID, ident: String, rolle: Rolle) = TODO("")
            override fun endreTildeling(saksid: UUID, ident: String, nyIdent: String, nyRolle: Rolle) = TODO("")
            override fun fjernTildeling(saksid: UUID, ident: String) = TODO("")
        }
    }
}
