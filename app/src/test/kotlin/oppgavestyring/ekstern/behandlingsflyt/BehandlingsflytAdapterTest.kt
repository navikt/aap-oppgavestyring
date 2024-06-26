package oppgavestyring.ekstern.behandlingsflyt

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import oppgavestyring.ekstern.behandlingsflyt.dto.*
import oppgavestyring.intern.oppgave.OppgaveService
import oppgavestyring.intern.oppgave.db.Oppgave
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime


fun generateBehandlingshistorikkRequest() = BehandlingshistorikkRequest(
    referanse = "asfdvdb",
    status = Behandlingstatus.OPPRETTET,
    saksnummer = "asrgsadrgdfgsw",
    personident = "12345678901",
    behandlingType = Behandlingstype.Førstegangsbehandling,
    opprettetTidspunkt = LocalDateTime.now(),
    avklaringsbehov = listOf(generateAvklaringsbehovRequest())
)

fun generateAvklaringsbehovRequest() = AvklaringsbehovDto(
    status = Avklaringsbehovstatus.OPPRETTET,
    definisjon = Definisjon("5003"),
    endringer = listOf(
        AvklaringsbehovhendelseEndring(
            status = Avklaringsbehovstatus.OPPRETTET,
            tidsstempel = LocalDateTime.now(),
            endretAv = "24342313426"
        )
    )
)

class BehandlingsflytAdapterTest {

    val oppgaveService = mockk<OppgaveService>()
    val behandlingsflytAdapter = BehandlingsflytAdapter(oppgaveService)

    @BeforeEach
    fun setup() {
        justRun { oppgaveService.lukkOppgaverPåBehandling(any()) }
    }


    @Test
    fun `mapBehandlingshistorikkTilOppgaveHendelser oppretter oppgave ved åpent avklaringsbehov`() {
        val request = generateBehandlingshistorikkRequest()
        every { oppgaveService.opprett(any(), any(), any(), any(), any(), any(), any()) } returns mockk<Oppgave>()

        behandlingsflytAdapter.mapBehandlingshistorikkTilOppgaveHendelser(request)


        verify(exactly = 1){ oppgaveService.opprett(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `mapBehandlingshistorikkTilOppgaveHendelser lukker oppgave når behandling er lukket`() {
        val request = generateBehandlingshistorikkRequest().copy(status = Behandlingstatus.AVSLUTTET,
            avklaringsbehov = emptyList()
        )
        justRun { oppgaveService.lukkOppgaverPåBehandling(any()) }


        behandlingsflytAdapter.mapBehandlingshistorikkTilOppgaveHendelser(request)


        verify(exactly = 1){ oppgaveService.lukkOppgaverPåBehandling(request.referanse) }
    }

    @Test
    fun `mapBehandlingshistorikkTilOppgaveHendelser lukker oppgave når behandling ikke har noen åpne avklaringsbehov`() {
        val request = generateBehandlingshistorikkRequest().copy(
            avklaringsbehov = listOf(generateAvklaringsbehovRequest().copy(status = Avklaringsbehovstatus.AVSLUTTET))
        )
        justRun { oppgaveService.lukkOppgaverPåBehandling(any()) }

        behandlingsflytAdapter.mapBehandlingshistorikkTilOppgaveHendelser(request)

        verify(exactly = 1){ oppgaveService.lukkOppgaverPåBehandling(request.referanse) }
    }

}