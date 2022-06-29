package no.nav.aap.app.axsys

import com.fasterxml.jackson.databind.DeserializationFeature
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
import no.nav.aap.ktor.client.AzureConfig
import no.nav.aap.ktor.client.HttpClientAzureAdTokenProvider
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

data class AxsysConfig(
    val url: URI,
    val scope: String,
)

private val log = LoggerFactory.getLogger(AxsysClient::class.java)
private val secureLog = LoggerFactory.getLogger("secureLog")

class AxsysClient(private val axsysConfig: AxsysConfig, azureConfig: AzureConfig) {
    private val tokenProvider = HttpClientAzureAdTokenProvider(azureConfig, axsysConfig.scope)
    private val cache = mutableMapOf<String, List<String>>()

    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout)
        install(HttpRequestRetry)
        install(Logging) {
            level = LogLevel.BODY
            logger = object : Logger {
                override fun log(message: String) = secureLog.info(message)
            }
        }
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            }
        }
    }

    suspend fun hentEnheter(ident: String): List<String> = cache[ident] ?: slåOppEnheter(ident).also { bruker ->
        cache[ident] = bruker
    }

    private suspend fun slåOppEnheter(ident: String): List<String> {
        val token = tokenProvider.getToken()
        return httpClient.get(axsysConfig.url.toURL()) {
            url {
                appendPathSegments("tilgang", ident)
            }
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", callId)
            header("Nav-Consumer-Id", "aap_oppgavestyring")
            bearerAuth(token)
        }.body<Tilganger>().enheter.map { it.enhetId }
    }


    private val callId: String get() = UUID.randomUUID().toString().also { log.info("calling pdl with call-id $it") }
}

data class Tilganger(val enheter: List<Enhet>)

data class Enhet(val enhetId: String, val navn: String, val temaer: List<String>?)