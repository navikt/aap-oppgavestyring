import com.papsign.ktor.openapigen.route.apiRouting
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import oppgavestyring.config.db.DB_CONFIG_PREFIX
import oppgavestyring.config.db.Flyway
import oppgavestyring.server
import oppgavestyring.testutils.TestConfig
import oppgavestyring.testutils.TestDatabase
import oppgavestyring.testutils.fakes.AzureFake
import oppgavestyring.testutils.fakes.OppgaveFake
import oppgavestyring.testutils.fakes.generateJwtToken
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val LOG: Logger = LoggerFactory.getLogger("TestApp")

fun main() {
    TestDatabase.start()
    Flyway.migrate(TestDatabase.getConnection())
    System.setProperty("${DB_CONFIG_PREFIX}_JDBC_URL", TestDatabase.connectionUrl)
    System.setProperty("${DB_CONFIG_PREFIX}_USERNAME", TestDatabase.username)
    System.setProperty("${DB_CONFIG_PREFIX}_PASSWORD", TestDatabase.password)

    val azure = AzureFake()
    val oppgave = OppgaveFake()
    val config = TestConfig(
        oppgavePort = oppgave.port,
        azurePort = azure.port
    )

    val jwt = generateJwtToken()
    LOG.info("testToken: $jwt")

    embeddedServer(Netty, port = 8083){
        server(config)
    }.start(wait = true)

}