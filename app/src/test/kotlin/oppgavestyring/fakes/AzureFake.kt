package oppgavestyring.fakes

import com.auth0.jwk.Jwk
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import oppgavestyring.port
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.LocalDateTime
import java.util.*

class AzureFake : AutoCloseable {
    private val server = embeddedServer(Netty, port = 0, module = Application::azure).apply { start() }
    val port: Int get() = server.port()
    override fun close() = server.stop(0, 0)
}

val rsaKeyPair = generateRsaKeyPair()
val privateKey = rsaKeyPair.private as RSAPrivateKey
val publicKey = rsaKeyPair.public as RSAPublicKey


fun generateRsaKeyPair(): KeyPair {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(512)
    return generator.generateKeyPair()
}

fun generateJwtToken() = JWT.create()
    .withAudience("oppgave")
    .withIssuer("nav")
    .withClaim("username", "Testy mcTester")
    .withSubject("1234567890")
    .withClaim("NAVident", "T120345")
    .withArrayClaim("groups", arrayOf("12353679-aa80-4e59-bb47-95e727bfe85c"))
    .withExpiresAt(Date(System.currentTimeMillis() + 60000))
    .withIssuedAt(Date(System.currentTimeMillis() - 60000))
    .sign(Algorithm.RSA256(publicKey, privateKey))

private fun Application.azure() {
    install(ContentNegotiation) {
        jackson {}
    }

    routing {
        get {
            call.respondText {
                ObjectMapper().writeValueAsString(
                    mapOf(
                        "keys" to listOf(
                            mapOf(
                                "kty" to publicKey.algorithm,
                                "kid" to "dd9c18e6-7001-4197-9c52-99e378052971",
                                "n" to Base64.getUrlEncoder().encodeToString(publicKey.modulus.toByteArray()),
                                "e" to Base64.getUrlEncoder().encodeToString(publicKey.publicExponent.toByteArray()),
                                "alg" to "RS256",
                            )
                        )
                    )
                )
            }
        }
    }
}