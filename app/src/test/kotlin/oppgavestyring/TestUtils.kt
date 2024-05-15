package oppgavestyring

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.ktor.client.auth.azure.AzureConfig
import oppgavestyring.config.db.DatabaseSingleton
import oppgavestyring.config.db.DbConfig
import oppgavestyring.fakes.AzureFake
import oppgavestyring.fakes.OppgaveFake
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.net.URI
import javax.sql.DataSource

class Fakes : AutoCloseable {
    val azure = AzureFake()
    val oppgave = OppgaveFake()

    internal val config = TestConfig(
        oppgavePort = oppgave.port,
        azurePort = azure.port
    )

    override fun close() {
        azure.close()
        oppgave.close()
    }
}

fun NettyApplicationEngine.port() =
    runBlocking { resolvedConnectors() }
        .first { it.type == ConnectorType.HTTP }
        .port

internal fun oppgavestyringWithFakes(test: suspend (Fakes, HttpClient) -> Unit) {
    Fakes().use { fakes ->
        testApplication {
            application {
                server(fakes.config)
            }

            val client = createClient {
                install(ContentNegotiation) {
                    jackson {
                        registerModules(JavaTimeModule())
                    }
                }
            }

            test(fakes, client)
        }
    }
}

internal class TestConfig(oppgavePort: Int, azurePort: Int) : Config(
    oppgave = OppgaveConfig(
        host = "http://localhost:$oppgavePort".let(::URI),
        scope = "",
    ),
    azure = AzureConfig(
        tokenEndpoint = "http://localhost:$azurePort/token",
        clientId = "",
        clientSecret = "",
        jwksUri = "",
        issuer = "",
    ),
)



object TestDatabase {

    val postgres = PostgreSQLContainer<Nothing>("postgres:16")
    val username get() = postgres.username
    val password get() = postgres.password
    val connectionUrl get() = postgres.jdbcUrl

    fun start() { postgres.start() }
    fun stop() { postgres.stop()}
    fun reset() { postgres.execInContainer(
        "psql",  "-U", "test", "-d", "test" , "-c", "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
    ) }
    fun getConnection(): DataSource{
        DatabaseSingleton.init(
            DbConfig(
            connectionURL = connectionUrl,
            username = username,
            password = password)
        )
        return DatabaseSingleton.connection!!
    }
}