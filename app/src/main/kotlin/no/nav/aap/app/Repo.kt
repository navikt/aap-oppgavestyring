package no.nav.aap.app

import no.nav.aap.app.dao.*
import no.nav.aap.app.db.DBTildeling
import no.nav.aap.app.frontendView.FrontendMottaker
import no.nav.aap.app.frontendView.FrontendPersonopplysninger
import no.nav.aap.app.frontendView.FrontendSøker
import no.nav.aap.app.modell.InnloggetBruker
import no.nav.aap.app.modell.Rolle
import org.slf4j.LoggerFactory
import java.util.*
import javax.sql.DataSource

internal class Repo(dataSource: DataSource) {

    private val søkerDao = SøkerDao(dataSource)
    private val personopplysningerDao = PersonopplysningerDao(dataSource)
    private val mottakerDao = MottakerDao(dataSource)
    private val sakDao = SakDao(dataSource)
    private val tildelingDao = TildelingDao(dataSource)

    internal fun hentSøker(personident: String) = søkerDao.select(listOf(personident))

    internal fun lagreSøker(frontendSøker: FrontendSøker) = søkerDao.insert(frontendSøker)

    internal fun hentSøkere() = søkerDao.select(InnloggetBruker("", listOf(), listOf()))

    internal fun slettSøker(personident: String) {
        søkerDao.delete(personident)
        personopplysningerDao.delete(personident)
        mottakerDao.delete(personident)
        sakDao.delete(personident)
        tildelingDao.delete(personident)
    }

    private val secureLog = LoggerFactory.getLogger("secureLog")

    internal fun lagrePersonopplysninger(fp: FrontendPersonopplysninger) = personopplysningerDao.insert(fp)
    internal fun hentPersonopplysninger(personident: String) = personopplysningerDao.select(personident).also {
        secureLog.info("hent personopplysninger for $personident fra db: $it")
    }

    internal fun lagreMottaker(frontendMottaker: FrontendMottaker) = mottakerDao.insert(frontendMottaker)

    internal fun tildelSak(saksid: UUID, ident: String, rolle: Rolle) {
        tildelingDao.insert(
            DBTildeling(
                saksid = saksid,
                ident = ident,
                rolle = rolle.name
            )
        )
    }

    internal fun endreTildeling(saksid: UUID, ident: String, nyIdent: String, nyRolle: Rolle) {
        fjernTildeling(saksid, ident)
        tildelSak(saksid, nyIdent, nyRolle)
    }

    internal fun fjernTildeling(saksid: UUID, ident: String) {
        tildelingDao.delete(saksid, ident)
    }
}
