package oppgavestyring.intern.oppgave.api

import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.patch
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import io.ktor.http.*
import io.ktor.server.auth.*
import oppgavestyring.LOG
import oppgavestyring.config.security.OppgavePrincipal
import oppgavestyring.intern.oppgave.NavIdent
import oppgavestyring.intern.oppgave.OppgaveService
import oppgavestyring.respondWithStatus
import org.jetbrains.exposed.sql.transactions.transaction

fun NormalOpenAPIRoute.oppgaver(oppgaveService: OppgaveService) {
    route("/oppgaver") {

        get<ListOppgaverRequest, OppgaverResponse> { call ->
            LOG.info("Forsøker å søke opp alle oppgaver tilknyttet AAP")

            val principal = pipeline.context.authentication.principal<OppgavePrincipal>()!!

            val filter = trekkUtFilterParametere(pipeline.context.parameters)

            val searchParams = OppgaveParams(
                filters = filter,
                sorting = call.sortering.mapValues { it.value.toSQLSortOrder() })

            LOG.info("search params: $searchParams")
            val oppgaver = transaction {
                val oppgaver = if (!searchParams.isEmpty())
                    oppgaveService.søk(principal, searchParams)
                else
                    oppgaveService.hentÅpneOppgaver(principal)

                oppgaver.map { OppgaveDto.fromOppgave(it) }
            }
            respond(OppgaverResponse(oppgaver))
        }

        route("/{id}") {
            get<OppgaverByIdRequest, OppgaveDto> { req ->
                LOG.info("Forsøker å hente én konkret oppgave")

                val id = req.id

                val oppgave = transaction {
                    val oppgave = oppgaveService.hent(
                        oppgaveId = id
                    )
                    OppgaveDto.fromOppgave(oppgave)
                }
                respondWithStatus(HttpStatusCode.OK, oppgave)

            }
        }

        route("/nesteOppgave") {
            get<ListOppgaverRequest, OppgaveDto> { req ->
                val principal = pipeline.context.authentication.principal<OppgavePrincipal>()!!
                LOG.info("Bruker ${principal.ident} etterspør neste oppgave")

                val searchParams = OppgaveParams(
                    filters = trekkUtFilterParametere(pipeline.context.parameters),
                    sorting = req.sortering.mapValues { it.value.toSQLSortOrder() })

                val response = transaction {
                    val oppgave = oppgaveService.hentNesteOppgave(principal, searchParams)
                    OppgaveDto.fromOppgave(oppgave)
                }

                respondWithStatus(HttpStatusCode.OK, response)
            }
        }

        route("/{id}/tildelRessurs") {
            patch<OppgaverByIdRequest, Any, TildelRessursRequest> { req, body ->
                LOG.info("Forsøker å tildele ressurs til oppgave")

                val id = req.id
                transaction {
                    oppgaveService.tildelOppgave(
                        id = id,
                        navIdent = NavIdent(body.navIdent)
                    )
                }
                respondWithStatus(HttpStatusCode.NoContent)
            }
        }


        route("/{id}/frigi") {
            patch<OppgaverByIdRequest, Any, String> { req, _ ->
                LOG.info("Forsøker å frigi ressurs fra oppgave")

                LOG.info("Token OK")

                val id = req.id

                LOG.info("Uthenting av ID OK")

                transaction {
                    oppgaveService.frigiRessursFraOppgave(
                        id = id,
                    )
                }

                respondWithStatus(HttpStatusCode.NoContent)
            }
        }
    }
}