package oppgavestyring.filter

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import oppgavestyring.TestDatabase
import oppgavestyring.config.db.DB_CONFIG_PREFIX
import oppgavestyring.config.db.Flyway
import oppgavestyring.oppgavestyringWithFakes
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FilterRouteApiTest {
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

    @BeforeEach
    fun setup() {
        TestDatabase.reset()
        Flyway.migrate(TestDatabase.getConnection())
    }

    @Test
    fun `create oppgavefilter`() {
        oppgavestyringWithFakes { _, client ->
            val filterDto = FilterDto(
                1,
                "Oppgave",
                "Dette er en oppgave",
                """{"hello": "world"}"""
            )

            client.post("/filter") {
                contentType(ContentType.Application.Json)
                setBody(filterDto)
            }
        }
        val filter = transaction {
            OppgaveFilter.all().firstOrNull()
        }

        assertThat(filter).isNotNull
    }

    @Test
    fun `fetch oppgavefilter`() {

        val expected = FilterDto(
            1,
            "Oppgave",
            "Dette er en oppgave",
            """{"hello": "world"}"""
        )
        transaction {
            OppgaveFilter.new {
                tittel = expected.tittel
                beskrivelse = expected.beskrivelse
                filter = expected.filter
                opprettetAv = "K112233"
            }
        }

        oppgavestyringWithFakes { _, client ->
            val actual = client.get("/filter") {
                accept(ContentType.Application.Json)
            }.body<List<FilterDto>>()

            assertThat(actual.first())
                .usingRecursiveComparison()
                .isEqualTo(expected)
        }

    }

    @Test
    fun `slett oppgavefilter`() {
        val filterId = transaction {
            OppgaveFilter.new {
                tittel = "Test"
                beskrivelse = "Testbeskrivelse"
                filter = "{}"
                opprettetAv = "K112233"
            }.id.value
        }

        oppgavestyringWithFakes { _, client ->
            client.delete("/filter/$filterId")
        }

        transaction {
            val actual = OppgaveFilter.findById(filterId)
            assertThat(actual).isNull()
        }
    }

}