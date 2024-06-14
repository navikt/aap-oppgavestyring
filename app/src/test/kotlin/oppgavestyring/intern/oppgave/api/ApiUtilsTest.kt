package oppgavestyring.intern.oppgave.api

import io.ktor.http.*
import oppgavestyring.ekstern.behandlingsflyt.dto.Avklaringsbehovtype
import org.assertj.core.api.Assertions
import org.jetbrains.exposed.sql.SortOrder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ApiUtilsTest{
    @Nested
    inner class Filtering {

        @Test
        fun `test that filterparams are correctly formatted`() {
            val key = "avklaringbehovtype"

            val paramBuilder = ParametersBuilder()
            paramBuilder.append("${filtrering}[$key]", Avklaringsbehovtype.FATTE_VEDTAK.toString())

            val result = trekkUtFilterParametere(paramBuilder.build())

            Assertions.assertThat(result.keys)
                .contains(key)

            Assertions.assertThat(result[key])
                .contains(Avklaringsbehovtype.FATTE_VEDTAK.name)
        }

        @Test
        fun `test that multiple filterparams are correctly formatted`() {
            val key = "avklaringbehovtype"

            val paramBuilder = ParametersBuilder()
            paramBuilder.append("${filtrering}[$key]", Avklaringsbehovtype.FATTE_VEDTAK.name)
            paramBuilder.append("${filtrering}[$key]", Avklaringsbehovtype.AVKLAR_STUDENT.name)

            val result = trekkUtFilterParametere(paramBuilder.build())

            Assertions.assertThat(result.keys)
                .contains(key)

            Assertions.assertThat(result[key])
                .contains(Avklaringsbehovtype.FATTE_VEDTAK.name)
                .contains(Avklaringsbehovtype.AVKLAR_STUDENT.name)
        }

        @Test
        fun `trekk ut filter-dictionary`() {
            val paramBuilder = ParametersBuilder()
            paramBuilder.append(filtrering + "[key1]", "asc")
            paramBuilder.append(filtrering + "[key1]", "asc2")
            paramBuilder.append(filtrering + "[key2]", "desc")
            val params = paramBuilder.build()

            val res = trekkUtFilterParametere(params)

            Assertions.assertThat(res.keys)
                .hasSize(2)
            Assertions.assertThat(res).isEqualTo(mapOf("key1" to listOf("asc", "asc2"), "key2" to listOf("desc")))
        }

    }

}