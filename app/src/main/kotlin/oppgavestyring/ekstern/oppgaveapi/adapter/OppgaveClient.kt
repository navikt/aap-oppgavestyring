package oppgavestyring.ekstern.oppgaveapi.adapter

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
import oppgavestyring.SECURE_LOG
import oppgavestyring.ekstern.oppgaveapi.OppgaveGateway
import oppgavestyring.intern.oppgave.OppgaveId
import org.slf4j.LoggerFactory
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
        val obo = azure.getClientCredentialToken(config.oppgave.scope)

        val response = client.post("$host/api/v1/oppgaver") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("X-Correlation-ID", UUID.randomUUID().toString())
            bearerAuth(obo)
            setBody(request)
        }

        return response.tryInto<OpprettResponse>().also {
            log.info("Oppgave opprettet: ${response.headers[HttpHeaders.Location]}")
        }
    }

    override suspend fun hent(
        token: Token,
        oppgaveId: OppgaveId,
    ): Result<OpprettResponse> {
        val obo = azure.getOnBehalfOfToken(config.oppgave.scope, token.asString())

        val response = client.get("$host/api/v1/oppgaver/${oppgaveId}") {
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
        request: PatchOppgaveRequest
    ): Result<OpprettResponse> {

        val obo = azure.getOnBehalfOfToken(config.oppgave.scope, token.asString())

        val response = client.patch("$host/api/v1/oppgaver/${oppgaveId}") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            header("X-Correlation-ID", UUID.randomUUID().toString())
            bearerAuth(obo)
            setBody(request)
        }

        return response.tryInto<OpprettResponse>().also {
            log.info("Oppgave endret: ${response.headers[HttpHeaders.Location]}")
        }
    }

    companion object {
        val log = LoggerFactory.getLogger(OppgaveClient::class.java)
    }
}

internal suspend inline fun <reified R : Any> HttpResponse.tryInto(): Result<R> {

    return when (status.value) {
        in 200..202 -> Result.success(body<R>())
        400 -> Result.failure(logWithError("Oppgave API svarer med ugyldig request"))
        401 -> Result.failure(logWithError("Oppgave API svarer med ugyldig token"))
        403 -> Result.failure(logWithError("Oppgave API svarer med ugyldig tilgang"))
        409 -> Result.failure(logWithError("Oppgave API har en oppdatert versjon av oppgaven"))
        else -> Result.failure(logWithError("Oppgave API returnerer en ukjent feilkode: ${status.value}"))
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
        OppgaveClient.log.error(msg, it)
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