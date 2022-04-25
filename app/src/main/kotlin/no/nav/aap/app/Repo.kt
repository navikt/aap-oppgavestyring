package no.nav.aap.app

import no.nav.aap.app.dao.SakDao
import no.nav.aap.app.dao.SøkerDao
import no.nav.aap.app.dao.TildelingDao
import no.nav.aap.app.db.DBTildeling
import no.nav.aap.app.frontendView.FrontendSøker
import no.nav.aap.app.modell.Rolle
import java.util.*
import javax.sql.DataSource

internal class Repo(dataSource: DataSource) {

    private val søkerDao = SøkerDao(dataSource)
    private val sakDao = SakDao(dataSource)
    private val tildelingDao = TildelingDao(dataSource)

    internal fun hentSøker(personident: String) = søkerDao.select(listOf(personident))

    internal fun lagreSøker(frontendSøker: FrontendSøker) = søkerDao.insert(frontendSøker)

    internal fun hentSøkere() = søkerDao.select()

    internal fun slettSøker(personident: String) {
        søkerDao.delete(personident)
        sakDao.delete(personident)
        tildelingDao.delete(personident)
    }

    internal fun tildelSak(saksid: UUID, ident: String, rolle: Rolle) {
        tildelingDao.insert(DBTildeling(
            saksid = saksid,
            ident = ident,
            rolle = rolle.name
        ))
    }

    internal fun endreTildeling(saksid: UUID, ident: String, nyIdent: String, nyRolle: Rolle) {
        fjernTildeling(saksid, ident)
        tildelSak(saksid, nyIdent, nyRolle)
    }

    internal fun fjernTildeling(saksid: UUID, ident: String) {
        tildelingDao.delete(saksid, ident)
    }
}
