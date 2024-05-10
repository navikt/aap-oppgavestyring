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

    fun getÅpentAvklaringsbehov() = avklaringsbehov.firstOrNull {it.status == Avklaringsbehovstatus.OPPRETTET ||
            it.status == Avklaringsbehovstatus.SENDT_TILBAKE_FRA_BESLUTTER}
}

enum class Behandlingstype{
    FØRSTEGANGSBEHANDLING,
    REVURDERINGER,
    KLAGE,
    ANKE,
    TILBAKEKREVING
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

enum class Avklaringsbehovtype(val beskrivelse: String) {
    MANUELT_SATT_PÅ_VENT_KODE("sdgdbdghbdgb"),
    AVKLAR_STUDENT_KODE("sdfbhsdnbdsfndfn"),
    AVKLAR_SYKDOM_KODE("sdfsdgbsdgbdsgbsd"),
    FASTSETT_ARBEIDSEVNE_KODE("segsdgsdfgsdgsd"),
    FRITAK_MELDEPLIKT_KODE("sdfhgsdhgsdfg"),
    AVKLAR_BISTANDSBEHOV_KODE("sfdbsdfgbsdfbsdfgvb"),
    VURDER_SYKEPENGEERSTATNING_KODE("dszfgbsdfgbsdfgsd"),
    FASTSETT_BEREGNINGSTIDSPUNKT_KODE("sdgthsdfhjfhmfhn"),
    FORESLÅ_VEDTAK_KODE("sdgbhsdfbnsdfb"),
    FATTE_VEDTAK_KODE("sdhsdgsdfgdg")
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