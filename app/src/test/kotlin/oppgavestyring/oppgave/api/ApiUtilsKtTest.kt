package oppgavestyring.oppgave.api

import io.ktor.http.*
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import org.assertj.core.api.Assertions
import org.jetbrains.exposed.sql.SortOrder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ApiUtilsKtTest{

    @Nested
    inner class Sorting {

        @Test
        fun `test that querryparam sorters are correctly formatted`() {
            val key = "behandlingsreferanse"

            val paramBuilder = ParametersBuilder()
            paramBuilder.set(SearchParams.sortering.name, "$key=asc")
            val result = parseUrlFiltering(paramBuilder.build())


            Assertions.assertThat(result.sorting.keys)
                .contains(key)

            Assertions.assertThat(result.sorting[key])
                .isEqualTo(SortOrder.ASC)
        }

        @Test
        fun `test that queryparam sorters are correctly ordered `() {
            val key1 = "behandlingsreferanse"
            val key2 = "behandlingOpprettetTidspunkt"

            val paramBuilder = ParametersBuilder()
            paramBuilder.append(SearchParams.sortering.name, "$key1=asc")
            paramBuilder.append(SearchParams.sortering.name, "$key2=desc")
            val result = parseUrlFiltering(paramBuilder.build())


            Assertions.assertThat(result.sorting.keys)
                .hasSize(2)

            Assertions.assertThat(result.sorting.keys.toList()[0])
                .isEqualTo(key1)

        }
    }

    @Nested
    inner class Filtering {

        @Test
        fun `test that filterparams are correctly formatted`() {
            val key = "avklaringbehovtype"

            val paramBuilder = ParametersBuilder()
            paramBuilder.append(SearchParams.filtrering.name, "$key=${Avklaringsbehovtype.FATTE_VEDTAK}")
            val result = parseUrlFiltering(paramBuilder.build())

            Assertions.assertThat(result.filters.keys)
                .contains(key)

            Assertions.assertThat(result.filters[key])
                .contains(Avklaringsbehovtype.FATTE_VEDTAK.name)
        }

        @Test
        fun `test that multiple filterparams are correctly formatted`() {
            val key = "avklaringbehovtype"


            val filterParameterBuilder = ParametersBuilder()
            val paramBuilder = ParametersBuilder()
            filterParameterBuilder.append(key, Avklaringsbehovtype.FATTE_VEDTAK.name)
            filterParameterBuilder.append(key, Avklaringsbehovtype.AVKLAR_STUDENT.name)
            paramBuilder.append(SearchParams.filtrering.name, filterParameterBuilder.build().formUrlEncode())

            val result = parseUrlFiltering(paramBuilder.build())


            Assertions.assertThat(result.filters.keys)
                .contains(key)

            Assertions.assertThat(result.filters[key])
                .contains(Avklaringsbehovtype.FATTE_VEDTAK.name)
                .contains(Avklaringsbehovtype.AVKLAR_STUDENT.name)


        }

    }

}