package no.nav.aap.app.dao

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.frontendView.FrontendSøker
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

    internal fun select() =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                SELECT data FROM soker
                """
            session.run(
                queryOf(query).map {
                    objectMapper.readValue<FrontendSøker>(it.string("data"))
                }.asList
            )
        }

    internal fun select(personidenter: List<String>) =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                SELECT data FROM soker WHERE personident IN (${personidenter.joinToString { "?" }})
                """
            session.run(
                queryOf(query, *personidenter.toTypedArray()).map {
                    objectMapper.readValue<FrontendSøker>(it.string("data"))
                }.asList
            )
        }

}
