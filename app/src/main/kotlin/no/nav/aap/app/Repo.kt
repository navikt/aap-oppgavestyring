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

interface Repository {
    fun hentSøker(personident: String): List<FrontendSøker>
    fun lagreSøker(frontendSøker: FrontendSøker)
    fun hentSøkere(): List<FrontendSøker>
    fun slettSøker(personident: String)
    fun lagrePersonopplysninger(fp: FrontendPersonopplysninger)
    fun hentPersonopplysninger(personident: String): FrontendPersonopplysninger?
    fun lagreMottaker(frontendMottaker: FrontendMottaker)
    fun tildelSak(saksid: UUID, ident: String, rolle: Rolle)
    fun endreTildeling(saksid: UUID, ident: String, nyIdent: String, nyRolle: Rolle)
    fun fjernTildeling(saksid: UUID, ident: String)
}

internal class Repo(dataSource: DataSource) : Repository {

    private val søkerDao = SøkerDao(dataSource)
    private val personopplysningerDao = PersonopplysningerDao(dataSource)
    private val mottakerDao = MottakerDao(dataSource)
    private val sakDao = SakDao(dataSource)
    private val tildelingDao = TildelingDao(dataSource)

    override fun hentSøker(personident: String) = søkerDao.select(listOf(personident), InnloggetBruker("", listOf(), listOf()))

    override fun lagreSøker(frontendSøker: FrontendSøker) = søkerDao.insert(frontendSøker)

    override fun hentSøkere(): List<FrontendSøker> = søkerDao.select(InnloggetBruker("", listOf(), listOf()))

    override fun slettSøker(personident: String) {
        søkerDao.delete(personident)
        personopplysningerDao.delete(personident)
        mottakerDao.delete(personident)
        sakDao.delete(personident)
        tildelingDao.delete(personident)
    }

    private val secureLog = LoggerFactory.getLogger("secureLog")

    override fun lagrePersonopplysninger(fp: FrontendPersonopplysninger) = personopplysningerDao.insert(fp)
    override fun hentPersonopplysninger(personident: String): FrontendPersonopplysninger? = personopplysningerDao.select(personident).also {
        secureLog.info("hent personopplysninger for $personident fra db: $it")
    }

    override fun lagreMottaker(frontendMottaker: FrontendMottaker) = mottakerDao.insert(frontendMottaker)

    override fun tildelSak(saksid: UUID, ident: String, rolle: Rolle) {
        tildelingDao.insert(
            DBTildeling(
                saksid = saksid,
                ident = ident,
                rolle = rolle.name
            )
        )
    }

    override fun endreTildeling(saksid: UUID, ident: String, nyIdent: String, nyRolle: Rolle) {
        fjernTildeling(saksid, ident)
        tildelSak(saksid, nyIdent, nyRolle)
    }

    override fun fjernTildeling(saksid: UUID, ident: String) {
        tildelingDao.delete(saksid, ident)
    }
}
