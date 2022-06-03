package no.nav.aap.app

import no.nav.aap.app.axsys.AxsysConfig
import no.nav.aap.app.db.DbConfig
import no.nav.aap.kafka.KafkaConfig
import no.nav.aap.ktor.client.AzureConfig
import java.net.URL

data class Config(
    val oauth: OAuthConfig,
    val kafka: KafkaConfig,
    val database: DbConfig,
    val azure: AzureConfig,
    val axsys: AxsysConfig
)

data class OAuthConfig(
    val azure: IssuerConfig,
    val roles: List<String>
)

data class IssuerConfig(
    val issuer: String,
    val audience: String,
    val jwksUrl: URL,
)
