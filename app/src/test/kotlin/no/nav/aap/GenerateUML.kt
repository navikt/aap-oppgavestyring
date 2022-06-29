package no.nav.aap

import no.nav.aap.app.Repository
import no.nav.aap.app.axsys.InnloggetBruker
import no.nav.aap.app.frontendView.FrontendMottaker
import no.nav.aap.app.frontendView.FrontendPersonopplysninger
import no.nav.aap.app.topology
import no.nav.aap.kafka.streams.topology.Mermaid
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File

internal class GenerateUML {

    @Test
    fun `generate topology UML`() {
        val topology = topology(repo)
        val graf = Mermaid.graph("oppgavestyring", topology)
        File("../doc/topologi.mermaid").apply { writeText(graf) }
    }

    private companion object {
        private val log = LoggerFactory.getLogger("GenerateUML")

        private val repo = object : Repository {
            override fun hentSøker(personident: String, innloggetBruker: InnloggetBruker) = error("Ikke implementert")
            override fun lagreSøker(personident: String, version: Int, søker: ByteArray) = error("Ikke implementert")
            override fun hentSøkere(innloggetBruker: InnloggetBruker) = error("Ikke implementert")
            override fun slettSøker(personident: String) = error("Ikke implementert")
            override fun lagrePersonopplysninger(fp: FrontendPersonopplysninger) = error("Ikke implementert")
            override fun hentPersonopplysninger(personident: String) = error("Ikke implementert")
            override fun lagreMottaker(frontendMottaker: FrontendMottaker) = error("Ikke implementert")
        }
    }
}
