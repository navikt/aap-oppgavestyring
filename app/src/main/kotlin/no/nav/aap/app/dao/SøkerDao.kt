package no.nav.aap.app.dao

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.frontendView.FrontendSøker
import no.nav.aap.app.modell.InnloggetBruker
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class SøkerDao(private val dataSource: DataSource) {
    companion object {
        private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    internal fun insert(søker: FrontendSøker) {
        sessionOf(dataSource).use { session ->
            session.transaction { tSession ->
                @Language("PostgreSQL")
                val query = """
                    INSERT INTO soker VALUES(:personident, :data::json)
                    ON CONFLICT ON CONSTRAINT unique_personident DO UPDATE SET data = :data::json
                    """
                tSession.run(
                    queryOf(
                        query, mapOf(
                            "personident" to søker.personident,
                            "data" to objectMapper.writeValueAsString(søker)
                        )
                    ).asUpdate
                )
            }
        }
    }

    internal fun select(innloggetBruker: InnloggetBruker) =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                SELECT s.data FROM soker s 
                INNER JOIN personopplysninger p ON s.personident = p.personident
                WHERE p.adressebeskyttelse IN (:roller)
                """
            session.run(
                queryOf(query, mapOf(
                    "roller" to innloggetBruker.adressebeskyttelseRoller().joinToString(",")
                )).map {
                    objectMapper.readValue<FrontendSøker>(it.string("data"))
                }.asList
            )
        }

    internal fun select(personidenter: List<String>, innloggetBruker: InnloggetBruker) =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                SELECT s.data FROM soker s 
                INNER JOIN personopplysninger p ON s.personident = p.personident
                WHERE s.personident IN (:identer)
                AND p.adressebeskyttelse IN (:roller)
                """
            session.run(
                queryOf(query, mapOf(
                    "identer" to personidenter.joinToString(","),
                    "roller" to innloggetBruker.adressebeskyttelseRoller().joinToString(",")
                )).map {
                    objectMapper.readValue<FrontendSøker>(it.string("data"))
                }.asList
            )
        }

    internal fun delete(personident: String) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                DELETE FROM soker WHERE personident = :personident
                """
            session.run(
                queryOf(query, mapOf("personident" to personident)).asExecute
            )
        }
    }
}
