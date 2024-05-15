package oppgavestyring.config.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import oppgavestyring.LOG
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

object DatabaseSingleton {

    var connection: DataSource? = null

    fun init(config: DbConfig) {
        LOG.info("Setting up database connection")
        if (connection != null) return

        connection = createHikariDataSource(config)
        Database.connect(connection!!)
        LOG.info("Database connection established")
    }

    fun migrate() {
        LOG.info("Migrating database")
        require(connection != null) {"Database connection is required"}
        connection?.let { Flyway.migrate(it) }
        LOG.info("Database connection completed")
    }

    private fun createHikariDataSource(dbConfig: DbConfig) = HikariDataSource(HikariConfig().apply {
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