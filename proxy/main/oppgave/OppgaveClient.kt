package oppgave

import SECURE_LOG
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*

class OppgaveClient(private val config: OppgaveConfig) {
    private val httpClient = HttpClient(CIO) {
        install(HttpRequestRetry)
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = SECURE_LOG.info(message)
            }
            level = LogLevel.ALL
        }
    }

    fun opprett(proxyRequest: String): Result<String> {
        TODO("Not yet implemented")
    }
}