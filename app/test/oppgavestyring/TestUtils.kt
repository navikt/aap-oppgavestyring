package oppgavestyring

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.ktor.client.auth.azure.AzureConfig
import oppgavestyring.fakes.AzureFake
import oppgavestyring.fakes.OppgaveFake
import java.net.URI

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
                    jackson {}
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