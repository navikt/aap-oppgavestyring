package oppgavestyring.config.db

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import oppgavestyring.TestDatabaseSingleton
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DatabaseSingletonTest {
    companion object {
        val database = TestDatabaseSingleton.getInstance()

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            database.start()
        }

        @AfterAll
        @JvmStatic
        fun teardown() { database.stop() }
    }

    @BeforeEach
    fun beforeEach() {
        database.reset()
    }

    private fun connectToDatabase() {
        DatabaseSingleton.init(DbConfig(
            connectionURL = database.connectionUrl,
            username = database.username,
            password = database.password))}

    @Test
    fun `verify working database connection`() {
        connectToDatabase()
    }

    @Test
    fun `verify schema creation form Exposed`() {
        class TestTable : Table() {
            val id: Column<Int> = integer("id").autoIncrement()
            val someString: Column<String> = varchar("someString", 100)
            val someTimeStamp: Column<Instant> = timestamp("someTimeStamp")

            override val primaryKey = PrimaryKey(id, name = "PK_TEST_ID")
        }
        connectToDatabase()
        val testTable = TestTable()

        transaction {
            SchemaUtils.create(testTable)

            val id = testTable.insert {
                it[someString] = "HELLLO!"
                it[someTimeStamp] = Clock.System.now()
            } get testTable.id

            assertEquals(id, 1)
        }
    }

    @Test
    fun `run flyway migration without any errors`() {
        connectToDatabase()
        Flyway.migrate(DatabaseSingleton.connection ?:
            throw IllegalStateException("Database connection is not initialized"))
        println("SDFSDFDF")
    }

}