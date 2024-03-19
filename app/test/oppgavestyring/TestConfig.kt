package oppgavestyring

import no.nav.aap.ktor.client.auth.azure.AzureConfig
import java.net.URI

class TestConfig(oppgavePort: Int, azurePort: Int) : Config(
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
