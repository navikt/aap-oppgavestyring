package oppgavestyring.ekstern.oppslag

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.aap.ktor.client.auth.azure.AzureAdTokenProvider
import no.nav.aap.ktor.client.auth.azure.AzureConfig
import oppgavestyring.Config
import oppgavestyring.OppgaveConfig
import oppgavestyring.OppslagConfig
import oppgavestyring.intern.oppgave.Personident
import oppgavestyring.testutils.fakes.OppslagFake
import oppgavestyring.testutils.oppgavestyringWithFakes
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.net.URI

class OppslagClientTest {


    @Test
    fun `navnoppslag i oppslag-tjenesten fungerer som forventet av oppslagClient`() {
        val azureMock: AzureAdTokenProvider = mockk()
        val oppslagFake = OppslagFake()

        coEvery { azureMock.getClientCredentialToken(any()) } returns "clientToken"

            val oppslagClient = OppslagClient(OppslagConfig(
                host = "http://localhost:${oppslagFake.port}".let(::URI),
                scope = ""
            ), azureMock)

            val actual = runBlocking {
                oppslagClient.hentNavnForPersonIdent(Personident("12345678912"))
            }

            Assertions.assertThat(actual.toString())
                .isEqualTo("Testy Testersen")

    }


}