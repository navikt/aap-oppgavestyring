package oppgavestyring.config.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import oppgavestyring.LOG
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

class DatabaseManager(databaseConfig: DatabaseConfiguration) {

    val connection: DataSource

    init {
        LOG.info("Setting up database connection")
        connection = createHikariDataSource(databaseConfig)
        Database.connect(connection)
        LOG.info("Database connection established")
        migrate()
    }

    fun migrate() {
        LOG.info("Migrating database")
        Flyway.migrate(connection)
        LOG.info("Database connection completed")
    }

    private fun createHikariDataSource(dbConfig: DatabaseConfiguration) = HikariDataSource(HikariConfig().apply {
        driverClassName = dbConfig.driver
        jdbcUrl = dbConfig.connectionURL
        username = dbConfig.username
        password = dbConfig.password
        maximumPoolSize = 10
        minimumIdle = 1
        initializationFailTimeout = 5000
        idleTimeout = 10001
        connectionTimeout = 1000
        maxLifetime = 30001
    })
}