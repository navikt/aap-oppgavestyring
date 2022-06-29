package no.nav.aap.app

import no.nav.aap.app.kafka.*
import java.time.LocalDate
import java.time.LocalDateTime

data class DtoLøsningInngangsvilkår(
    val løsning_11_2: DtoLøsningParagraf_11_2?,
    val løsning_11_3: DtoLøsningParagraf_11_3,
    val løsning_11_4: DtoLøsningParagraf_11_4_ledd2_ledd3?,
)

data class DtoLøsningParagraf_11_2(
    val erMedlem: String
) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_2_manuell(
        vurdertAv = vurdertAv,
        tidspunktForVurdering = LocalDateTime.now(),
        erMedlem = erMedlem
    )
}

data class DtoLøsningParagraf_11_3(
    val erOppfylt: Boolean
) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_3_manuell(
        vurdertAv = vurdertAv,
        tidspunktForVurdering = LocalDateTime.now(),
        erOppfylt = erOppfylt
    )
}

data class DtoLøsningParagraf_11_4_ledd2_ledd3(
    val erOppfylt: Boolean
) {
    internal fun toKafkaDto(vurdertAv: String) =
        Løsning_11_4_ledd2_ledd3_manuell(
            vurdertAv = vurdertAv,
            tidspunktForVurdering = LocalDateTime.now(),
            erOppfylt = erOppfylt
        )
}

data class DtoLøsningParagraf_11_5(
    val kravOmNedsattArbeidsevneErOppfylt: Boolean,
    val nedsettelseSkyldesSykdomEllerSkade: Boolean
) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_5_manuell(
        vurdertAv = vurdertAv,
        tidspunktForVurdering = LocalDateTime.now(),
        kravOmNedsattArbeidsevneErOppfylt = kravOmNedsattArbeidsevneErOppfylt,
        nedsettelseSkyldesSykdomEllerSkade = nedsettelseSkyldesSykdomEllerSkade
    )
}

data class DtoLøsningParagraf_11_6(
    val harBehovForBehandling: Boolean,
    val harBehovForTiltak: Boolean,
    val harMulighetForÅKommeIArbeid: Boolean
) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_6_manuell(
        vurdertAv = vurdertAv,
        tidspunktForVurdering = LocalDateTime.now(),
        harBehovForBehandling = harBehovForBehandling,
        harBehovForTiltak = harBehovForTiltak,
        harMulighetForÅKommeIArbeid = harMulighetForÅKommeIArbeid
    )
}

data class DtoLøsningParagraf_11_12_ledd1(
    val bestemmesAv: String,
    val unntak: String,
    val unntaksbegrunnelse: String,
    val manueltSattVirkningsdato: LocalDate
) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_12_ledd1_manuell(
        vurdertAv = vurdertAv,
        tidspunktForVurdering = LocalDateTime.now(),
        bestemmesAv = bestemmesAv,
        unntak = unntak,
        unntaksbegrunnelse = unntaksbegrunnelse,
        manueltSattVirkningsdato = manueltSattVirkningsdato
    )
}

data class DtoLøsningParagraf_11_19(
    val beregningsdato: LocalDate
) {
    internal fun toKafkaDto(vurdertAv: String) =
        Løsning_11_19_manuell(
            vurdertAv = vurdertAv,
            tidspunktForVurdering = LocalDateTime.now(),
            beregningsdato = beregningsdato
        )
}

data class DtoLøsningParagraf_11_29(
    val erOppfylt: Boolean
) {
    internal fun toKafkaDto(vurdertAv: String) = Løsning_11_29_manuell(
        vurdertAv = vurdertAv,
        tidspunktForVurdering = LocalDateTime.now(),
        erOppfylt = erOppfylt
    )
}
