package oppgavestyring

import no.nav.aap.ktor.client.auth.azure.AzureConfig
import java.net.URI

class TestConfig : Config(
    oppgave = OppgaveConfig(
        host = "http://oppgave".let(::URI),
        scope = "scope,"
    ),
    azure = AzureConfig(
        tokenEndpoint = "http://azure",
        clientId = "",
        clientSecret = "",
        jwksUri = "",
        issuer = "",
    ),
)
