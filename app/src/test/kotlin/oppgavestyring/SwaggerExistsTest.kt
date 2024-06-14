package oppgavestyring

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import oppgavestyring.config.db.DB_CONFIG_PREFIX
import oppgavestyring.config.db.Flyway
import oppgavestyring.testutils.TestDatabase
import oppgavestyring.testutils.oppgavestyringWithFakes
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class SwaggerExistsTest {
    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            TestDatabase.start()
            Flyway.migrate(TestDatabase.getConnection())

            System.setProperty("${DB_CONFIG_PREFIX}_JDBC_URL", TestDatabase.connectionUrl)
            System.setProperty("${DB_CONFIG_PREFIX}_USERNAME", TestDatabase.username)
            System.setProperty("${DB_CONFIG_PREFIX}_PASSWORD", TestDatabase.password)
        }
    }

    @Test
    fun `checks that openapi-json is served without errors`() {
        oppgavestyringWithFakes { _, client ->
            val actual = client.get("/openapi.json") {}

            Assertions.assertEquals(HttpStatusCode.OK, actual.status)
            Assertions.assertNotNull(actual.bodyAsText())
        }
    }

    @Test
    fun `checks that openapi-html is served without errors`() {
        oppgavestyringWithFakes { _, client ->
            val actual = client.get("/swagger-ui/index.html") {}

            Assertions.assertEquals(HttpStatusCode.OK, actual.status)
            Assertions.assertNotNull(actual.bodyAsText())
        }
    }
}