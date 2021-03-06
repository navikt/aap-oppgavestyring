package no.nav.aap.app.frontendView

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class FrontendSak(
    val saksid: UUID,
    val søknadstidspunkt: LocalDateTime,
    val type: String,
    val vedtak: FrontendVedtak?,
    val inngangsvilkår: Inngangsvilkår?,
    @Deprecated("Flyttes til inngangsvilkår")
    val paragraf_11_2: FrontendParagraf_11_2?,
    @Deprecated("Flyttes til inngangsvilkår")
    val paragraf_11_3: FrontendParagraf_11_3?,
    @Deprecated("Flyttes til inngangsvilkår")
    val paragraf_11_4: FrontendParagraf_11_4?,
    val paragraf_11_5: FrontendParagraf_11_5?,
    val paragraf_11_6: FrontendParagraf_11_6?,
    val paragraf_11_12: FrontendParagraf_11_12?,
    val paragraf_11_19: FrontendParagraf_11_19?,
    val paragraf_11_29: FrontendParagraf_11_29?,
    @Deprecated("Erstattes av paragraf_11_19")
    val beregningsdato: FrontendBeregningsdato?
)

enum class Autorisasjon {
    LESE, ENDRE, GODKJENNE
}

enum class Utfall {
    OPPFYLT, IKKE_OPPFYLT, IKKE_VURDERT, IKKE_RELEVANT
}

data class Inngangsvilkår(
    val autorisasjon: Autorisasjon,
    val paragraf_11_2: FrontendParagraf_11_2?,
    val paragraf_11_3: FrontendParagraf_11_3?,
    val paragraf_11_4: FrontendParagraf_11_4?,
)

data class FrontendParagraf_11_2(
    val vilkårsvurderingsid: UUID,
    val utfall: String,
    @Deprecated("Erstattes av autorisasjon i inngangsvilkår")
    val autorisasjon: Autorisasjon
)

data class FrontendParagraf_11_3(
    val vilkårsvurderingsid: UUID,
    val utfall: String,
    @Deprecated("Erstattes av autorisasjon i inngangsvilkår")
    val autorisasjon: Autorisasjon
)

data class FrontendParagraf_11_4(
    val vilkårsvurderingsid: UUID,
    val utfall: String,
    @Deprecated("Erstattes av autorisasjon i inngangsvilkår")
    val autorisasjon: Autorisasjon
)

data class FrontendParagraf_11_5(
    val vilkårsvurderingsid: UUID,
    val utfall: String,
    val autorisasjon: Autorisasjon,
    val kravOmNedsattArbeidsevneErOppfylt: Boolean?,
    val nedsettelseSkyldesSykdomEllerSkade: Boolean?
)

data class FrontendParagraf_11_6(
    val vilkårsvurderingsid: UUID,
    val utfall: String,
    val autorisasjon: Autorisasjon,
    val harBehovForBehandling: Boolean?,
    val harBehovForTiltak: Boolean?,
    val harMulighetForÅKommeIArbeid: Boolean?
)

data class FrontendParagraf_11_12(
    val vilkårsvurderingsid: UUID,
    val utfall: String,
    val autorisasjon: Autorisasjon,
    val bestemmesAv: String?,
    val unntak: String?,
    val unntaksbegrunnelse: String?,
    val manueltSattVirkningsdato: LocalDate?
)

data class FrontendParagraf_11_19(
    val vilkårsvurderingsid: UUID,
    val utfall: String,
    val autorisasjon: Autorisasjon,
    val beregningsdato: LocalDate?,
)

data class FrontendParagraf_11_29(
    val vilkårsvurderingsid: UUID,
    val utfall: String,
    val autorisasjon: Autorisasjon
)

data class FrontendBeregningsdato(
    val beregningsdato: LocalDate?,
    val autorisasjon: Autorisasjon
)
