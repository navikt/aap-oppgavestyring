package no.nav.aap.app

import no.nav.aap.app.axsys.InnloggetBruker
import no.nav.aap.app.dao.MottakerDao
import no.nav.aap.app.dao.PersonopplysningerDao
import no.nav.aap.app.dao.SøkerDao
import no.nav.aap.app.frontendView.FrontendMottaker
import no.nav.aap.app.frontendView.FrontendPersonopplysninger
import no.nav.aap.app.frontendView.FrontendSøker
import no.nav.aap.app.frontendView.toFrontendView
import org.slf4j.LoggerFactory
import javax.sql.DataSource

interface Repository {
    fun hentSøker(personident: String, innloggetBruker: InnloggetBruker): List<FrontendSøker>
    fun lagreSøker(personident: String, version: Int, søker: ByteArray)
    fun hentSøkere(innloggetBruker: InnloggetBruker): List<FrontendSøker>
    fun slettSøker(personident: String)
    fun lagrePersonopplysninger(fp: FrontendPersonopplysninger)
    fun hentPersonopplysninger(personident: String): FrontendPersonopplysninger?
    fun lagreMottaker(frontendMottaker: FrontendMottaker)
}

internal class Repo(dataSource: DataSource) : Repository {

    private val søkerDao = SøkerDao(dataSource)
    private val personopplysningerDao = PersonopplysningerDao(dataSource)
    private val mottakerDao = MottakerDao(dataSource)

    override fun hentSøker(personident: String, innloggetBruker: InnloggetBruker) =
        søkerDao.select(listOf(personident), innloggetBruker).map { it.toFrontendView(innloggetBruker) }

    override fun lagreSøker(personident: String, version: Int, søker: ByteArray) =
        søkerDao.insert(personident, version, søker)

    override fun hentSøkere(innloggetBruker: InnloggetBruker) =
        søkerDao.select(innloggetBruker).map { it.toFrontendView(innloggetBruker) }

    override fun slettSøker(personident: String) {
        personopplysningerDao.delete(personident)
        søkerDao.delete(personident)
        mottakerDao.delete(personident)
    }

    private val secureLog = LoggerFactory.getLogger("secureLog")

    override fun lagrePersonopplysninger(fp: FrontendPersonopplysninger) = personopplysningerDao.insert(fp)
    override fun hentPersonopplysninger(personident: String): FrontendPersonopplysninger? =
        personopplysningerDao.select(personident).also {
            secureLog.info("hent personopplysninger for $personident fra db: $it")
        }

    override fun lagreMottaker(frontendMottaker: FrontendMottaker) = mottakerDao.insert(frontendMottaker)
}
