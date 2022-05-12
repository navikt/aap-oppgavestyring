package no.nav.aap.app.dao

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.frontendView.FrontendMottaker
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class MottakerDao(private val dataSource: DataSource) {
    companion object {
        private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    internal fun insert(mottaker: FrontendMottaker) {
        sessionOf(dataSource).use { session ->
            session.transaction { tSession ->
                @Language("PostgreSQL")
                val query = """
                    INSERT INTO mottaker VALUES(:personident, :data::json)
                    ON CONFLICT ON CONSTRAINT unique_mottaker_personident DO UPDATE SET data = :data::json
                    """
                tSession.run(
                    queryOf(
                        query, mapOf(
                            "personident" to mottaker.personident,
                            "data" to objectMapper.writeValueAsString(mottaker)
                        )
                    ).asUpdate
                )
            }
        }
    }

    internal fun select() =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                SELECT data FROM mottaker
                """
            session.run(
                queryOf(query).map {
                    objectMapper.readValue<FrontendMottaker>(it.string("data"))
                }.asList
            )
        }

    internal fun select(personidenter: List<String>) =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                SELECT data FROM mottaker WHERE personident IN (${personidenter.joinToString { "?" }})
                """
            session.run(
                queryOf(query, *personidenter.toTypedArray()).map {
                    objectMapper.readValue<FrontendMottaker>(it.string("data"))
                }.asList
            )
        }

    internal fun delete(personident: String) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                DELETE FROM mottaker WHERE personident = :personident
                """
            session.run(
                queryOf(query, mapOf("personident" to personident)).asExecute
            )
        }
    }
}
