package no.nav.aap.app

import no.nav.aap.app.dao.SakDao
import no.nav.aap.app.dao.SøkerDao
import no.nav.aap.app.frontendView.FrontendSøker
import javax.sql.DataSource

internal class Repo(dataSource: DataSource) {

    private val søkerDao = SøkerDao(dataSource)
    private val sakDao = SakDao(dataSource)

    internal fun hentSøker(personident: String) = søkerDao.select(listOf(personident))

    internal fun lagreSøker(frontendSøker: FrontendSøker) = søkerDao.insert(frontendSøker)

    internal fun hentSøkere() = søkerDao.select()

    internal fun slettSøker(personident: String) {
        søkerDao.delete(personident)
        sakDao.delete(personident)
    }
}
