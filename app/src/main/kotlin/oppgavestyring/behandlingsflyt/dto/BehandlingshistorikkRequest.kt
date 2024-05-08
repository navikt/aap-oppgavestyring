package oppgavestyring.behandlingsflyt.dto

import java.time.LocalDateTime

data class BehandlingshistorikkRequest(
    val personident: String,
    val saksnummer: String,
    val behandlingsreferanse: String,
    val behandlingType: Behandlingstype,
    val status: Behandlingstatus,
    val avklaringsbehov: List<AvklaringsbehovHendelseDto>,
    val opprettetTidspunkt: LocalDateTime
) {
    fun erLukket() =
        (status == Behandlingstatus.AVSLUTTET) ||
                avklaringsbehov.all { it.status == Avklaringsbehovstatus.AVSLUTTET }
    fun getÅpentAvklaringsbehov() = avklaringsbehov.firstOrNull {it.status != Avklaringsbehovstatus.AVSLUTTET && it.status != Avklaringsbehovstatus.AVBRUTT}
}

enum class Behandlingstype{

    Førstegangsbehandling,
    Revurdering,
    Tilbakekreving,
    Klage

}

enum class Behandlingstatus {
    OPPRETTET,
    UTREDES,
    AVSLUTTET,
    PÅ_VENT
}

enum class Avklaringsbehovstatus {
    OPPRETTET,
    AVSLUTTET,
    TOTRINNS_VURDERT,
    SENDT_TILBAKE_FRA_BESLUTTER,
    AVBRUTT
}

enum class Avklaringsbehovtype {
    MANUELT_SATT_PÅ_VENT_KODE,
    AVKLAR_STUDENT_KODE,
    AVKLAR_SYKDOM_KODE,
    FASTSETT_ARBEIDSEVNE_KODE,
    FRITAK_MELDEPLIKT_KODE,
    AVKLAR_BISTANDSBEHOV_KODE,
    VURDER_SYKEPENGEERSTATNING_KODE,
    FASTSETT_BEREGNINGSTIDSPUNKT_KODE,
    FORESLÅ_VEDTAK_KODE,
    FATTE_VEDTAK_KODE
}

data class AvklaringsbehovHendelseDto(
    val type: Avklaringsbehovtype,
    val status: Avklaringsbehovstatus,
    val endringer: List<Avklaringsbehovhendelse>
) {

}

data class Avklaringsbehovhendelse(
    val status: Avklaringsbehovstatus,
    val tidsstempel: LocalDateTime,
    val endretAv: String
)

enum class Oppgavestatus {
    ÅPEN,
    AVSLUTTET,
    PÅ_VENT
}