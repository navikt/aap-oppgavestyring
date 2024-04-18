package oppgavestyring.oppgave.adapter

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
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import no.nav.aap.ktor.client.auth.azure.AzureAdTokenProvider
import oppgavestyring.Config
import oppgavestyring.LOG
import oppgavestyring.SECURE_LOG
import oppgavestyring.oppgave.OppgaveGateway
import oppgavestyring.oppgave.OppgaveId
import java.util.*

data class Token(private val token: String) {
    fun asString() : String = token
}

class OppgaveClient(private val config: Config) : OppgaveGateway {
    private val client = HttpClientFactory.default()
    private val azure = AzureAdTokenProvider(config.azure)
    private val host = config.oppgave.host

    override suspend fun opprett(
        token: Token,
        request: OpprettRequest,
    ): Result<OpprettResponse> {
        val obo = azure.getOnBehalfOfToken(config.oppgave.scope, token.asString())

        val response = client.post("$host/api/v1/oppgaver") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("X-Correlation-ID", UUID.randomUUID().toString())
            bearerAuth(obo)
            setBody(request)
        }

        return response.tryInto<OpprettResponse>().also {
            LOG.info("Oppgave opprettet: ${response.headers[HttpHeaders.Location]}")
        }
    }

    override suspend fun hent(
        token: Token,
        oppgaveId: OppgaveId,
    ): Result<OpprettResponse> {
        val obo = azure.getOnBehalfOfToken(config.oppgave.scope, token.asString())

        val response = client.get("$host/api/v1/oppgaver/${oppgaveId.asLong()}") {
            accept(ContentType.Application.Json)
            header("X-Correlation-ID", UUID.randomUUID().toString())
            bearerAuth(obo)
        }

        return response.tryInto()
    }

    override suspend fun søk(
        token: Token,
        params: SøkQueryParams,
    ): Result<SøkOppgaverResponse> {
        val clientCredential = azure.getClientCredentialToken(config.oppgave.scope)

        val response = client.get {
            url {
                takeFrom("${config.oppgave.host}/api/v1/oppgaver")
                parameters.appendAll(params.stringValues())
            }
            accept(ContentType.Application.Json)
            header("X-Correlation-ID", UUID.randomUUID().toString())
            bearerAuth(clientCredential)
        }

        return response.tryInto()
    }

    override suspend fun endre(
        token: Token,
        oppgaveId: OppgaveId,
        request: PatchOppgaveRequest): Result<OpprettResponse> {

        val obo = azure.getOnBehalfOfToken(config.oppgave.scope, token.asString())

        val response = client.patch("$host/api/v1/oppgaver/${oppgaveId.asLong()}") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("X-Correlation-ID", UUID.randomUUID().toString())
            bearerAuth(obo)
            setBody(request)
        }

        return response.tryInto<OpprettResponse>().also {
            LOG.info("Oppgave endret: ${response.headers[HttpHeaders.Location]}")
        }
    }
}

internal suspend inline fun <reified R : Any> HttpResponse.tryInto(): Result<R> {

    return when (status.value) {
        in 200..202 -> Result.success(body<R>())
        400 -> Result.failure(logWithError("Ugyldig request (oppgave)"))
        401 -> Result.failure(logWithError("Ugyldig token (oppgave)"))
        403 -> Result.failure(logWithError("Ugyldig tilgang (oppgave)"))
        else -> Result.failure(logWithError("Ukjent feil (oppgave)"))
    }
}

private fun HttpResponse.logWithError(msg: String): IllegalStateException {
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
        """.trimIndent()
    ).also {
        LOG.error(msg, it)
    }
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