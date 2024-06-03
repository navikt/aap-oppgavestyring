package oppgavestyring.config

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import no.nav.aap.ktor.client.auth.azure.AzureConfig
import oppgavestyring.SECURE_LOG
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit

val NAV_IDENT_CLAIM_NAME = "NAVident"

const val AZURE = "azure"
fun Application.authentication(config: AzureConfig) {

    val jwkProvider = JwkProviderBuilder(URI.create(config.jwksUri).toURL()).cached(10, 24, TimeUnit.HOURS)
        .rateLimited(10, 1, TimeUnit.MINUTES).build()

    authentication {
        jwt(AZURE) {
            verifier(jwkProvider, config.issuer)
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "AzureAD validering feilet") }
            validate { cred ->
                val now = Date()
                SECURE_LOG.info("Ident of requester: ${cred.getClaim(NAV_IDENT_CLAIM_NAME, String::class)}")
                SECURE_LOG.info("Groups of requester: ${cred.getListClaim("groups", String::class)}")
                if (config.clientId !in cred.audience) {
                    SECURE_LOG.warn("AzureAD validering feilet (clientId var ikke i audience: ${cred.audience}")
                    return@validate null
                }

                if (cred.expiresAt?.before(now) == true) {
                    SECURE_LOG.warn("AzureAD validering feilet (expired at: ${cred.expiresAt})")
                    return@validate null
                }

                if (cred.notBefore?.after(now) == true) {
                    SECURE_LOG.warn("AzureAD validering feilet (not valid yet, valid from: ${cred.notBefore})")
                    return@validate null
                }

                if (cred.issuedAt?.after(cred.expiresAt ?: return@validate null) == true) {
                    SECURE_LOG.warn("AzureAD validering feilet (issued after expiration: ${cred.issuedAt} )")
                    return@validate null
                }

                JWTPrincipal(cred.payload)
            }
        }
    }
}