package oppgavestyring.oppgave

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith


class NavIdentTest {

    @Test
    fun `navIdent kan ikke være blank`() {
        val assertFailsWith = assertFailsWith<IllegalArgumentException> {
            NavIdent("")
        }

        assertThat(assertFailsWith.message).isEqualTo("Feil format på navIdent: . Riktig format er stor bokstav og 6 tall.")
    }

    @Test
    fun `navIdent kan ikke bestå av flere enn 7 tegn`() {
        val assertFailsWith = assertFailsWith<IllegalArgumentException> {
            NavIdent("D1134242")
        }

        assertThat(assertFailsWith.message).isEqualTo("Feil format på navIdent: D1134242. Riktig format er stor bokstav og 6 tall.")
    }

    @Test
    fun `navIdent må bestå av en bokstav og 6 tall`() {
        val navIdent = NavIdent("D113442")
        assertThat(navIdent.asString()).isEqualTo("D113442")
    }

    @Test
    fun `navIdent kan ikke bestå av et ekstra tegn foran`() {
        val assertFailsWith = assertFailsWith<IllegalArgumentException> {
            NavIdent("DD113442")
        }

        assertThat(assertFailsWith.message).isEqualTo("Feil format på navIdent: DD113442. Riktig format er stor bokstav og 6 tall.")
    }

    @Test
    fun `navIdent kan ikke bestå av et ekstra tegn til slutt`() {
        val assertFailsWith = assertFailsWith<IllegalArgumentException> {
            NavIdent("D1134423")
        }

        assertThat(assertFailsWith.message).isEqualTo("Feil format på navIdent: D1134423. Riktig format er stor bokstav og 6 tall.")
    }
}