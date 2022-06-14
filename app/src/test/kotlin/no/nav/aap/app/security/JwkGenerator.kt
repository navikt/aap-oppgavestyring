package no.nav.aap.app.security

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.*
import java.util.concurrent.TimeUnit


object JwtGenerator {
    private val jwkSet: JWKSet get() = JWKSet.parse(this::class.java.getResource("/jwkset.json")!!.readText())
    private val rsaKey: RSAKey get() = jwkSet.getKeyByKeyId("localhost-signer") as RSAKey

    fun generateSaksbehandlerToken(): SignedJWT = createSignedJWT(rsaKey, claims(listOf(TestAzureGroups.SAKSBEHANDLER)))

    private fun createSignedJWT(rsaJwk: RSAKey, claimsSet: JWTClaimsSet): SignedJWT {
        val header = JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJwk.keyID).type(JOSEObjectType.JWT).build()
        val signer: JWSSigner = RSASSASigner(rsaJwk.toPrivateKey())
        return SignedJWT(header, claimsSet).apply {
            sign(signer)
        }
    }

    private fun claims( groups: List<TestAzureGroups>, now: Date = Date()) = JWTClaimsSet.Builder()
        .subject(null)
        .issuer("azure")
        .audience("oppgavestyring")
        .jwtID(UUID.randomUUID().toString())
        .claim("groups", groups.map { it.uuid })
        .claim("NAVident", "Z000001")
        .claim("preferred_username", "test.test@test.com")
        .notBeforeTime(now)
        .issueTime(now)
        .expirationTime(Date(now.time + TimeUnit.MINUTES.toMillis((60 * 60 * 3600).toLong()))).build()
}

enum class TestAzureGroups(val uuid: String) {
    SAKSBEHANDLER("9eea5eb0-1f42-4661-949a-91740d817f49"),
    BESLUTTER("bcc57777-aba4-45ef-8f07-fa594e54a33f"),
    VEILEDER("bcc57777-aba4-45ef-8f07-fa594e54a44c"),
    FATTER("bcc57777-aba4-45ef-8f07-fa594e54a55a"),
    LES("bcc57777-aba4-45ef-8f07-fa594e54a66b"),
    FORTROLIG_ADRESSE("bcc57777-aba4-45ef-8f07-fa594e54b22c"),
    STRENGT_FORTROLIG_ADRESSE("bcc57555-aba4-45ef-8f07-fa594e54b22c")
}