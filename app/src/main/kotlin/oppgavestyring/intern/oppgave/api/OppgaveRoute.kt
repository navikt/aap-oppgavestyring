package oppgavestyring.intern.oppgave.api

import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.path.normal.patch
import io.ktor.http.*
import io.ktor.server.auth.*
import oppgavestyring.LOG
import oppgavestyring.config.security.OppgavePrincipal
import oppgavestyring.intern.oppgave.NavIdent
import oppgavestyring.intern.oppgave.OppgaveService
import oppgavestyring.respondWithStatus
import org.jetbrains.exposed.sql.transactions.transaction

data class OppgaverByIdRequest(@PathParam(description = "xxxx") val id: Long)


fun NormalOpenAPIRoute.oppgaver(oppgaveService: OppgaveService) {
    route("/oppgaver") {

        get<ListOppgaverRequest, OppgaverResponse> { call ->
            LOG.info("Forsøker å søke opp alle oppgaver tilknyttet AAP")

            ///val searchParams = call.queryParam
            // todo sjekk claims??
            val principal = pipeline.context.authentication.principal<OppgavePrincipal>()!!

            val searchParams = OppgaveParams(filters = call.filters, sorting = call.sorting)
            val oppgaver = transaction {
                val oppgaver = if (!searchParams.isEmpty())
                    oppgaveService.søk(principal, searchParams)
                else
                    oppgaveService.hentÅpneOppgaver(principal)

                oppgaver.map { OppgaveDto.fromOppgave(it) }
            }
            respondWithStatus(HttpStatusCode.OK, OppgaverResponse(oppgaver))
        }

        route("/{id}") {
            get<OppgaverByIdRequest, OppgaveDto> { req ->
                LOG.info("Forsøker å hente én konkret oppgave")

                val id = req.id
                //?: return@get call.respond(HttpStatusCode.BadRequest, "mangler path-param 'id'")

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
                LOG.info("Bruker ${principal.ident.toString()} etterspørr neste oppgave")

                //val searchParams = req.queryParam
                val searchParams = OppgaveParams(filters = emptyMap(), sorting = emptyMap())

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

                // todo sjekk parsing av string
                val id = req.id
                    //?: return@patch call.respond(HttpStatusCode.BadRequest, "mangler path-param 'id'")

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
            patch<OppgaverByIdRequest, Any, Any>() { req, _ ->
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

