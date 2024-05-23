package oppgavestyring.oppgave.api

import io.ktor.http.*
import oppgavestyring.behandlingsflyt.dto.Avklaringsbehovtype
import oppgavestyring.oppgave.db.OppgaveTabell
import org.assertj.core.api.Assertions
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.SortOrder
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

class ApiUtilsKtTest{

    @Nested
    inner class Sorting {

        @Test
        fun `test that querryparam sorters are correctly formatted`() {
            val key = "behandlingsreferanse"

            val paramBuilder = ParametersBuilder()
            paramBuilder.set(SearchParams.sortering.name, "$key=asc")
            val result = parseUrlFiltering<OppgaveTabell>(paramBuilder.build())

            val prop = OppgaveTabell::class.memberProperties.find { it.name == key }!! as KProperty1<OppgaveTabell, Column<Any>>

            Assertions.assertThat(result.sorting.keys)
                .contains(prop)

            Assertions.assertThat(result.sorting[prop])
                .isEqualTo(SortOrder.ASC)
        }

        @Test
        fun `test that queryparam sorters are correctly ordered `() {
            val key1 = "behandlingsreferanse"
            val key2 = "behandlingOpprettetTidspunkt"

            val paramBuilder = ParametersBuilder()
            paramBuilder.append(SearchParams.sortering.name, "$key1=asc")
            paramBuilder.append(SearchParams.sortering.name, "$key2=desc")
            val result = parseUrlFiltering<OppgaveTabell>(paramBuilder.build())


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
            val result = parseUrlFiltering<OppgaveTabell>(paramBuilder.build())

            val prop = OppgaveTabell::class.memberProperties.find { it.name == key }!! as KProperty1<OppgaveTabell, Column<Any>>

            Assertions.assertThat(result.filters.keys)
                .contains(prop)

            Assertions.assertThat(result.filters[prop])
                .contains(Avklaringsbehovtype.FATTE_VEDTAK.name)
        }

        @Test
        fun `test that multiple filterparams are correctly formatted`() {
            val key = "avklaringbehovtype"


            val paramBuilder = ParametersBuilder()
            paramBuilder.append(SearchParams.filtrering.name, "$key=${Avklaringsbehovtype.FATTE_VEDTAK}")
            paramBuilder.append(SearchParams.filtrering.name, "$key=${Avklaringsbehovtype.AVKLAR_STUDENT}")

            val result = parseUrlFiltering<OppgaveTabell>(paramBuilder.build())

            val prop = OppgaveTabell::class.memberProperties.find { it.name == key }!! as KProperty1<OppgaveTabell, Column<Any>>

            Assertions.assertThat(result.filters.keys)
                .contains(prop)

            Assertions.assertThat(result.filters[prop])
                .contains(Avklaringsbehovtype.FATTE_VEDTAK.name)
                .contains(Avklaringsbehovtype.AVKLAR_STUDENT.name)


        }

    }

}