package oppgavestyring.proxy

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.ktor.client.auth.azure.AzureAdTokenProvider
import oppgavestyring.Config
import oppgavestyring.SECURE_LOG

interface Oppgave {
    suspend fun opprett(token: String, request: OpprettRequest): Result<String>
}

class OppgaveClient(private val config: Config) : Oppgave {
    private val client = HttpClientFactory.default()
    private val azure = AzureAdTokenProvider(config.azure)

    override suspend fun opprett(
        token: String,
        request: OpprettRequest,
    ): Result<String> {
        val obo = azure.getOnBehalfOfToken(config.oppgave.scope, token)

        val response = client.post("${config.oppgave.host}/opprettoppgave") {
            accept(ContentType.Application.Json)
            header("personident", request.fnr.fnr)
            bearerAuth(obo)
            setBody(request)
        }

        return response.tryInto { "Oppgave opprettet" }
    }
}

private suspend fun HttpResponse.tryInto(default: () -> String): Result<String> {
    return when (status.value) {
        in 200..299 -> Result.success(default())
        in 400..499 -> Result.failure(logWithError("Client error: ${bodyAsText()}"))
        in 500..599 -> Result.failure(logWithError("Server error: ${bodyAsText()}"))
        else -> Result.failure(logWithError("Unknown error"))
    }
}

fun HttpResponse.logWithError(msg: String): IllegalStateException {
    SECURE_LOG.error(
        """
            $msg
            Request: ${request.method.value} ${request.url}
            Response: $status
            Headers: $headers
            Body: ${runBlocking { bodyAsText() }}
        """.trimIndent()
    )

    return IllegalStateException(
        """
        $msg
        Request: ${request.method.value} ${request.url}
        Response: $status
        Headers: $headers
    """.trimIndent()
    )
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
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
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
