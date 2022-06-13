package no.nav.aap.app.dao

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.aap.app.RoleName
import no.nav.aap.app.axsys.InnloggetBruker
import no.nav.aap.app.frontendView.FrontendSøker
import no.nav.aap.app.kafka.SøkereKafkaDto
import org.intellij.lang.annotations.Language
import javax.sql.DataSource

internal class SøkerDao(private val dataSource: DataSource) {
    companion object {
        private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    internal fun insert(søker: SøkereKafkaDto) {
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

    private fun List<String>.toSqlArray(session: Session) =
        session.connection.underlying.createArrayOf("text", toTypedArray())

    internal fun select(innloggetBruker: InnloggetBruker): List<SøkereKafkaDto> =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                SELECT s.data
                FROM soker s
                         INNER JOIN personopplysninger p ON s.personident = p.personident
                WHERE p.adressebeskyttelse = ANY (:roller)
                  AND (:harSkjermingsrolle OR (p.er_skjermet_fom IS NULL OR p.er_skjermet_fom > now() OR
                                               (p.er_skjermet_tom IS NOT NULL AND p.er_skjermet_tom < now())))
                  AND (:erTilknyttetNAY OR (:erTilknyttetLokalkontor AND p.norg_enhet_id = ANY (:tilknyttedeEnheter)))
                """
            session.run(
                queryOf(
                    query, mapOf(
                        "roller" to innloggetBruker.adressebeskyttelseRoller().map(RoleName::name).toSqlArray(session),
                        "harSkjermingsrolle" to innloggetBruker.harSkjermingsrolle(),
                        "erTilknyttetNAY" to innloggetBruker.erTilknyttetNAY(),
                        "erTilknyttetLokalkontor" to innloggetBruker.erTilknyttetLokalkontor(),
                        "tilknyttedeEnheter" to innloggetBruker.tilknyttedeEnheter().toSqlArray(session),
                    )
                ).map {
                    objectMapper.readValue<SøkereKafkaDto>(it.string("data"))
                }.asList
            )
        }

    internal fun select(personidenter: List<String>, innloggetBruker: InnloggetBruker) =
        sessionOf(dataSource).use { session ->
            @Language("PostgreSQL")
            val query = """
                SELECT s.data
                FROM soker s
                         INNER JOIN personopplysninger p ON s.personident = p.personident
                WHERE s.personident = ANY (:identer)
                  AND p.adressebeskyttelse = ANY (:roller)
                  AND (:harSkjermingsrolle OR (p.er_skjermet_fom IS NULL OR p.er_skjermet_fom > now() OR
                                               (p.er_skjermet_tom IS NOT NULL AND p.er_skjermet_tom < now())))
                  AND (:erTilknyttetNAY OR (:erTilknyttetLokalkontor AND p.norg_enhet_id = ANY (:tilknyttedeEnheter)))
                """
            session.run(
                queryOf(
                    query, mapOf(
                        "identer" to personidenter.toSqlArray(session),
                        "roller" to innloggetBruker.adressebeskyttelseRoller().map(RoleName::name).toSqlArray(session),
                        "harSkjermingsrolle" to innloggetBruker.harSkjermingsrolle(),
                        "erTilknyttetNAY" to innloggetBruker.erTilknyttetNAY(),
                        "erTilknyttetLokalkontor" to innloggetBruker.erTilknyttetLokalkontor(),
                        "tilknyttedeEnheter" to innloggetBruker.tilknyttedeEnheter().toSqlArray(session),
                    )
                ).map {
                    objectMapper.readValue<SøkereKafkaDto>(it.string("data"))
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
