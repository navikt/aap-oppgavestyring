package oppgavestyring.config.db


import oppgavestyring.TestDatabase
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Instant

class DatabaseManagerTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            TestDatabase.start()
        }
    }

    @AfterEach
    fun afterEach() {
        TestDatabase.reset()
    }

    val databaseManager = DatabaseManager(DatabaseConfiguration(
            connectionURL = TestDatabase.connectionUrl,
            username = TestDatabase.username,
            password = TestDatabase.password))

    @Test
    fun `verify schema creation form Exposed`() {
        class TestTable : Table() {
            val id: Column<Int> = integer("id").autoIncrement()
            val someString: Column<String> = varchar("someString", 100)
            val someTimeStamp: Column<Instant> = timestamp("someTimeStamp")

            override val primaryKey = PrimaryKey(id, name = "PK_TEST_ID")
        }

        val testTable = TestTable()

        transaction {
            SchemaUtils.create(testTable)

            val id = testTable.insert {
                it[someString] = "HELLLO!"
                it[someTimeStamp] = Instant.now()
            } get testTable.id

            assertEquals(id, 1)
        }
    }

    @Test
    fun `run flyway migration without any errors`() {
        TestDatabase.reset()
        Flyway.migrate(databaseManager.connection)
    }

}