package no.nav.aap.app

import no.nav.aap.app.axsys.AxsysConfig
import no.nav.aap.app.db.DbConfig
import no.nav.aap.kafka.streams.KStreamsConfig
import no.nav.aap.ktor.client.AzureConfig
import java.net.URL
import java.util.*

data class Config(
    val oauth: OAuthConfig,
    val kafka: KStreamsConfig,
    val database: DbConfig,
    val azure: AzureConfig,
    val axsys: AxsysConfig,
)

data class OAuthConfig(
    val azure: IssuerConfig,
    val roles: List<Role>,
)

data class Role(
    val name: RoleName,
    val objectId: UUID,
)

data class IssuerConfig(
    val issuer: String,
    val audience: String,
    val jwksUrl: URL,
)

enum class RoleName {
    SAKSBEHANDLER, BESLUTTER, VEILEDER, FATTER, LES, FORTROLIG_ADRESSE, STRENGT_FORTROLIG_ADRESSE, UGRADERT
}
