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

        assertThat(assertFailsWith.message).isEqualTo("navIdent kan ikke være blank")
    }

    @Test
    fun `navIdent kan ikke bestå av flere enn 7 tegn`() {
        val assertFailsWith = assertFailsWith<IllegalArgumentException> {
            NavIdent("D1134242")
        }

        assertThat(assertFailsWith.message).isEqualTo("navIdent må være under 8 tegn")
    }

    @Test
    fun `navIdent må bestå av en bokstav og 6 tall`() {
        val navIdent = NavIdent("D113442")
        assertThat(navIdent.asString()).isEqualTo("D113442")
    }
}