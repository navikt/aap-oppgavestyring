package oppgavestyring.ekstern.oppslag

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.ktor.client.auth.azure.AzureAdTokenProvider
import oppgavestyring.Config
import oppgavestyring.OppslagConfig
import oppgavestyring.SECURE_LOG
import oppgavestyring.intern.oppgave.Personident

class OppslagClient(private val oppslagConfig: OppslagConfig, private val azureAdTokenProvider: AzureAdTokenProvider) {
    private val client = HttpClientFactory.default()

    fun hentNavnForPersonIdent(personident: Personident)= runBlocking {
            val obo = azureAdTokenProvider.getClientCredentialToken(oppslagConfig.scope)

            client.get("${oppslagConfig.host}/navn") {
            headers["personident"] = personident.toString()
            accept(ContentType.Application.Json)
            bearerAuth(obo)
        }.body<NavnDto>() }
    }

private object HttpClientFactory {
    fun default(): HttpClient = HttpClient(CIO) {
        install(HttpRequestRetry)

        install(Logging) {
            logger = SecureLog.INFO
            level = LogLevel.ALL
        }

        install(ContentNegotiation) {
            jackson {
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                setSerializationInclusion(JsonInclude.Include.ALWAYS)
                registerModule(JavaTimeModule())
            }
        }
    }

    object SecureLog {
        object INFO : Logger {
            override fun log(message: String) = SECURE_LOG.info(message)
        }
    }
}