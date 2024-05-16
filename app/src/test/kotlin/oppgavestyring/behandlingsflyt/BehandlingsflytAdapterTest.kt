package oppgavestyring.behandlingsflyt

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import oppgavestyring.behandlingsflyt.dto.*
import oppgavestyring.oppgave.OppgaveService
import oppgavestyring.oppgave.db.Oppgave
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


    @Test
    fun `mapBehnadlingshistorikkTilOppgaveHendelser oppretter oppgave ved åpent avklaringsbehov`() {
        val request = generateBehandlingshistorikkRequest()
        every { oppgaveService.opprett_v2(any(), any(), any(), any(), any(), any(), any()) } returns mockk<Oppgave>()

        behandlingsflytAdapter.mapBehnadlingshistorikkTilOppgaveHendelser(request)


        verify(exactly = 1){ oppgaveService.opprett_v2(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `mapBehnadlingshistorikkTilOppgaveHendelser lukker oppgave når behandling er lukket`() {
        val request = generateBehandlingshistorikkRequest().copy(status = Behandlingstatus.AVSLUTTET,
            avklaringsbehov = emptyList()
        )
        justRun { oppgaveService.lukkOppgave(any()) }


        behandlingsflytAdapter.mapBehnadlingshistorikkTilOppgaveHendelser(request)


        verify(exactly = 1){ oppgaveService.lukkOppgave(request.referanse) }
    }

    @Test
    fun `mapBehnadlingshistorikkTilOppgaveHendelser lukker oppgave når behandling ikke har noen åpne avklaringsbehov`() {
        val request = generateBehandlingshistorikkRequest().copy(
            avklaringsbehov = listOf(generateAvklaringsbehovRequest().copy(status = Avklaringsbehovstatus.AVSLUTTET))
        )
        justRun { oppgaveService.lukkOppgave(any()) }

        behandlingsflytAdapter.mapBehnadlingshistorikkTilOppgaveHendelser(request)

        verify(exactly = 1){ oppgaveService.lukkOppgave(request.referanse) }
    }

}