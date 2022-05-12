package no.nav.aap.app.dao

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.frontendView.FrontendPersonopplysninger
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class PersonopplysningerDao(private val dataSource: DataSource) {
    companion object {
        private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    internal fun insert(fp: FrontendPersonopplysninger) {
        sessionOf(dataSource).use { session ->
            session.transaction { tSession ->
                @Language("PostgreSQL")
                val query = """
                    INSERT INTO personopplysninger VALUES(:personident, :data::json)
                    ON CONFLICT ON CONSTRAINT personoppl_unique_personident DO UPDATE SET data = :data::json
                    """
                tSession.run(
                    queryOf(
                        query, mapOf(
                            "personident" to fp.personident,
                            "data" to objectMapper.writeValueAsString(fp)
                        )
                    ).asUpdate
                )
            }
        }
    }

    internal fun select(ident: String) = sessionOf(dataSource).use { session ->
        @Language("PostgreSQL")
        val query = """
                SELECT data FROM personopplysninger WHERE personident = :personident
                """
        session.run(
            queryOf(query, mapOf("personident" to ident)).map {
                objectMapper.readValue<FrontendPersonopplysninger>(it.string("data"))
            }.asSingle
        )
    }

    internal fun select(personidenter: List<String>) = sessionOf(dataSource).use { session ->
        @Language("PostgreSQL")
        val query = """
                SELECT data FROM personopplysninger WHERE personident IN (${personidenter.joinToString { "?" }})
                """
        session.run(
            queryOf(query, *personidenter.toTypedArray()).map {
                objectMapper.readValue<FrontendPersonopplysninger>(it.string("data"))
            }.asList
        )
    }

    internal fun delete(personident: String) {
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                DELETE FROM personopplysninger WHERE personident = :personident
                """
            session.run(
                queryOf(query, mapOf("personident" to personident)).asExecute
            )
        }
    }
}
