package oppgavestyring.behandlingsflyt

import io.mockk.*
import kotlinx.coroutines.runBlocking
import oppgavestyring.behandlingsflyt.dto.*
import oppgavestyring.oppgave.OppgaveService
import org.junit.jupiter.api.Test

import java.time.LocalDateTime


fun generateBehandlingshistorikkRequest() = BehandlingshistorikkRequest(
    behandlingsreferanse = "sdghehsdfh",
    opprettetTidspunkt = LocalDateTime.now(),
    status = Behandlingstatus.OPPRETTET,
    saksnummer = "asrgsadrgdfgsw",
    personident = "12345678901",
    behandlingType = Behandlingstype.FØRSTEGANGSBEHANDLING,
    avklaringsbehov = listOf(generateAvklaringsbehovRequest())
)

fun generateAvklaringsbehovRequest() = AvklaringsbehovHendelseDto(
    status = Avklaringsbehovstatus.OPPRETTET,
    type = Avklaringsbehovtype.AVKLAR_SYKDOM_KODE,
    endringer = emptyList()
)

class BehandlingsflytAdapterTest {

    val oppgaveService = mockk<OppgaveService>()
    val behandlingsflytAdapter = BehandlingsflytAdapter(oppgaveService)


    @Test
    fun `mapBehnadlingshistorikkTilOppgaveHendelser oppretter oppgave ved åpent avklaringsbehov`() {
        val request = generateBehandlingshistorikkRequest()
        coJustRun { oppgaveService.opprett_v2(any(), any(), any()) }

        runBlocking {
            behandlingsflytAdapter.mapBehnadlingshistorikkTilOppgaveHendelser(request)
        }

        coVerify(exactly = 1){ oppgaveService.opprett_v2(any(), any(), any() ) }
    }

    @Test
    fun `mapBehnadlingshistorikkTilOppgaveHendelser lukker oppgave når behandling er lukket`() {
        val request = generateBehandlingshistorikkRequest().copy(status = Behandlingstatus.AVSLUTTET,
            avklaringsbehov = emptyList()
        )
        coJustRun { oppgaveService.lukkOppgave(any()) }

        runBlocking {
            behandlingsflytAdapter.mapBehnadlingshistorikkTilOppgaveHendelser(request)
        }

        coVerify(exactly = 1){ oppgaveService.lukkOppgave(request.behandlingsreferanse) }
    }

    @Test
    fun `mapBehnadlingshistorikkTilOppgaveHendelser lukker oppgave når behandling ikke har noen åpne avklaringsbehov`() {
        val request = generateBehandlingshistorikkRequest().copy(
            avklaringsbehov = listOf(generateAvklaringsbehovRequest().copy(status = Avklaringsbehovstatus.AVSLUTTET))
        )
        coJustRun { oppgaveService.lukkOppgave(any()) }

        runBlocking {
            behandlingsflytAdapter.mapBehnadlingshistorikkTilOppgaveHendelser(request)
        }

        coVerify(exactly = 1){ oppgaveService.lukkOppgave(request.behandlingsreferanse) }
    }

}