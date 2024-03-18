package oppgave

import java.net.URI

class Config(
    val oppgave: OppgaveConfig = OppgaveConfig(),
)

data class OppgaveConfig(
    val host: URI = "https://oppgave.dev-fss-pub.nais.io".let(::URI),
    val scope: String = "api://dev-fss.oppgavehandtering.oppgave-q2/.default",
)

