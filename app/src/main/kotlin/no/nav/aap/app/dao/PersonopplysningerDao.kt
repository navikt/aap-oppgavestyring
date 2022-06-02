package no.nav.aap.app.dao

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotliquery.Row
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
                    INSERT INTO personopplysninger VALUES(
                        :personident, 
                        :norg_enhet_id, 
                        :adressebeskyttelse, 
                        :geografisk_tilknytning,
                        :er_skjermet,
                        :er_skjermet_fom,
                        :er_skjermet_tom
                    )
                    ON CONFLICT ON CONSTRAINT personoppl_unique_personident DO UPDATE SET 
                        norg_enhet_id = :norg_enhet_id,
                        adressebeskyttelse = :adressebeskyttelse, 
                        geografisk_tilknytning = :geografisk_tilknytning,
                        er_skjermet = :er_skjermet,
                        er_skjermet_fom = :er_skjermet_fom,
                        er_skjermet_tom = :er_skjermet_tom
                    """
                tSession.run(
                    queryOf(
                        query, mapOf(
                            "personident" to fp.personident,
                            "norg_enhet_id" to fp.norgEnhetId,
                            "adressebeskyttelse" to fp.adressebeskyttelse,
                            "geografisk_tilknytning" to fp.geografiskTilknytning,
                            "er_skjermet" to fp.erSkjermet,
                            "er_skjermet_fom" to fp.erSkjermetFom,
                            "er_skjermet_tom" to fp.erSkjermetTom
                        )
                    ).asUpdate
                )
            }
        }
    }

    internal fun select(ident: String) = sessionOf(dataSource).use { session ->
        @Language("PostgreSQL")
        val query = """
                SELECT * FROM personopplysninger WHERE personident = :personident
                """
        session.run(
            queryOf(query, mapOf("personident" to ident)).map(::frontendPersonopplysninger).asSingle
        )
    }

    internal fun select(personidenter: List<String>) = sessionOf(dataSource).use { session ->
        @Language("PostgreSQL")
        val query = """
                SELECT * FROM personopplysninger WHERE personident IN (${personidenter.joinToString { "?" }})
                """
        session.run(
            queryOf(query, *personidenter.toTypedArray()).map(::frontendPersonopplysninger).asList
        )
    }

    private fun frontendPersonopplysninger(row: Row) = FrontendPersonopplysninger(
        personident = row.string("personident"),
        norgEnhetId = row.string("norg_enhet_id"),
        adressebeskyttelse = row.string("adressebeskyttelse"),
        geografiskTilknytning = row.string("geografisk_tilknytning"),
        erSkjermet = row.boolean("er_skjermet"),
        erSkjermetFom = row.localDateOrNull("er_skjermet_fom"),
        erSkjermetTom = row.localDateOrNull("er_skjermet_tom")
    )

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
