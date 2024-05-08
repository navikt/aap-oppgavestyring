package oppgavestyring.config.db

import org.flywaydb.core.Flyway
import javax.sql.DataSource

object Flyway {
    fun migrate(dataSource: DataSource) = Flyway
            .configure()
            .cleanDisabled(false) // TODO: husk å skru av denne før prod
            .cleanOnValidationError(true) // TODO: husk å skru av denne før prod
            .dataSource(dataSource)
            .locations("flyway")
            .validateMigrationNaming(true)
            .load()
            .migrate()
}
