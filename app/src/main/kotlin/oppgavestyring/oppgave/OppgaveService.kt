package oppgavestyring.oppgave

import oppgavestyring.LOG
import oppgavestyring.oppgave.Oppgavetype.BEHANDLE_SAK
import oppgavestyring.oppgave.adapter.*
import java.time.LocalDate

class OppgaveService(private val oppgaveRepository: OppgaveRepository, private val oppgaveGateway: OppgaveGateway) {

    suspend fun opprett_v2(token: Token, personident: Personident, beskrivelse: String): Result<OpprettResponse> {
        val request = OpprettRequest(
            oppgavetype = BEHANDLE_SAK.kode(),
            prioritet = Prioritet.NORM,
            aktivDato = LocalDate.now().toString(),
            personident = personident.asString(),
            beskrivelse = beskrivelse,
            opprettetAvEnhetsnr = "9999",
            behandlesAvApplikasjon = "KELVIN"
        )

        val nyOppgave = oppgaveGateway.opprett(
            token = token,
            request = request
        )

        nyOppgave.onSuccess {
            oppgaveRepository.lagre(it)
        }

        return nyOppgave
    }

    suspend fun tildelRessursTilOppgave(id: OppgaveId, versjon: Versjon, navIdent: NavIdent, token: Token): Result<OpprettResponse> {
        val endretOppgave = oppgaveGateway.endre(
            token = token,
            oppgaveId = id,
            request = PatchOppgaveRequest(
                versjon = versjon.asLong(),
                tilordnetRessurs = navIdent.asString()
            )
        )

        endretOppgave.onSuccess { LOG.info("versjon: ${it.versjon} tilordnetRessurs: ${it.tilordnetRessurs}") }

        return endretOppgave
    }

    suspend fun frigiRessursFraOppgave(id: OppgaveId, versjon: Versjon, token: Token): Result<OpprettResponse> {
        val endretOppgave = oppgaveGateway.endre(
            token = token,
            oppgaveId = id,
            request = PatchOppgaveRequest(
                versjon = versjon.asLong(),
                tilordnetRessurs = null
            )
        )

        endretOppgave.onSuccess { LOG.info("versjon: ${it.versjon} tilordnetRessurs: ${it.tilordnetRessurs}") }

        return endretOppgave
    }

    suspend fun søk(token: Token): Result<SøkOppgaverResponse> {
        return oppgaveGateway.søk(
            token = token,
            params = SøkQueryParams(
                tema = listOf("AAP"),
                statuskategori = Statuskategori.AAPEN,
            )
        )
    }

    suspend fun hent(token: Token, oppgaveId: OppgaveId): Result<OpprettResponse> {
        return oppgaveGateway.hent(
            token = token,
            oppgaveId = oppgaveId)
    }

    suspend fun lukkOppgave(behandlingsreferanse: String) {
        // TODO impolement
        throw NotImplementedError("Kan ikke lukke oppgave enda")
    }

}