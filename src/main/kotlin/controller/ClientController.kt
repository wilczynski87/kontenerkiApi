package com.kontenery.controller

import com.kontenery.data.Client
import com.kontenery.data.finance.ClientFinanceDto
import com.kontenery.data.worker.WorkerCreateRequest
import com.kontenery.data.utils.historyStart
import com.kontenery.data.utils.startOfCurrentYear
import com.kontenery.service.ClientService
import com.kontenery.service.WorkerService
import com.kontenery.utils.ApiErrorResponse
import com.kontenery.utils.respondBadRequest
import com.kontenery.utils.respondInternalError
import io.ktor.http.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
fun Route.clientRoute(clientService: ClientService, workerService: WorkerService) {
    route("/client") {

        post {
            try {
                val client: Client = call.receive<Client>()
                val saveClient: Client? = clientService.save(client)
                if (saveClient != null) call.respond(saveClient.toDto())
                else call.respond(HttpStatusCode.ExpectationFailed, ApiErrorResponse("Failed to save client"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, ApiErrorResponse(e.message ?: "Client already exists"))
            } catch (e: Exception) {
                call.respondInternalError(e, "Failed to save client")
            }
        }

        post("/employees") {
            try {
                val clientId = requireCustomerClientId(call)
                val body = call.receive<WorkerCreateRequest>()
                val created = workerService.createWorkerForClient(clientId, body)
                call.respond(HttpStatusCode.Created, created)
            } catch (e: IllegalArgumentException) {
                call.respondBadRequest(e.message ?: "Invalid employee data")
            } catch (e: Exception) {
                when (e) {
                    is BadRequestException -> throw e
                    else -> call.respondInternalError(e, "Failed to create employee")
                }
            }
        }

        get("/employees") {
            try {
                val clientId = requireCustomerClientId(call)
                val workers = workerService.listWorkersForClient(clientId)
                call.respond(workers) // DTO still uses Worker naming internally
            } catch (e: Exception) {
                when (e) {
                    is BadRequestException -> throw e
                    else -> call.respondInternalError(e, "Failed to list employees")
                }
            }
        }

        delete("/employees/{workerId}") {
            try {
                val clientId = requireCustomerClientId(call)
                val workerId = call.pathParameters["workerId"]?.toLongOrNull()
                    ?: throw BadRequestException("Invalid employee ID format")

                val deleted = workerService.deleteWorkerForClient(clientId, workerId)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, ApiErrorResponse("Employee not found"))
                }
            } catch (e: Exception) {
                when (e) {
                    is BadRequestException -> throw e
                    else -> call.respondInternalError(e, "Failed to delete employee")
                }
            }
        }

        get("/employees/{workerId}/gate-events") {
            try {
                val clientId = requireCustomerClientId(call)
                val workerId = call.pathParameters["workerId"]?.toLongOrNull()
                    ?: throw BadRequestException("Invalid employee ID format")
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100

                val events = workerService.listGateEventsForWorker(clientId, workerId, limit)
                call.respond(events)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.NotFound, ApiErrorResponse(e.message ?: "Employee not found"))
            } catch (e: Exception) {
                when (e) {
                    is BadRequestException -> throw e
                    else -> call.respondInternalError(e, "Failed to list gate events")
                }
            }
        }

        get("/findAll") {
            val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100
            val clients: List<Client> = clientService.getAllClients(page, size)
            call.respond(clients.map(Client::toDto))
        }

        get("/{id}/id") {
            val id: Long = call.request.pathVariables["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID format")
            val client: Client? = clientService.findClientById(id)

            if (client == null) {
                call.respond(HttpStatusCode.NotFound, ApiErrorResponse("Client not found"))
            } else {
                call.respond(client.toDto())
            }
        }

        put("/{id}") {
            try {
                val pathId = call.pathParameters["id"]?.toLongOrNull()
                    ?: throw BadRequestException("Invalid ID format")

                val clientUpdate = call.receive<Client>()
                if (clientUpdate.id != null && clientUpdate.id != pathId) {
                    throw BadRequestException("Client ID in body does not match path ID")
                }

                val updatedClient = clientService.updateClient(clientUpdate.copy(id = pathId))
                    ?: throw NotFoundException("Client not found")

                call.respond(updatedClient.toDto())
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, ApiErrorResponse(e.message ?: "Client already exists"))
            } catch (e: Exception) {
                when (e) {
                    is BadRequestException, is NotFoundException -> throw e
                    else -> call.respondInternalError(e, "Failed to update client")
                }
            }
        }

        post("/fromDb") {
            try {
                val clients: List<Client> = call.receive<List<Client>>()
                clients.forEach { clientService.save(it) }
                call.respond(HttpStatusCode.OK, ApiErrorResponse("Import completed"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, ApiErrorResponse(e.message ?: "Client already exists"))
            } catch (e: Exception) {
                call.respondInternalError(e, "Failed to import clients")
            }
        }

        get("/finanseForClient/{id}") {
            try {
                val id = call.pathParameters["id"]?.toLongOrNull()
                    ?: throw BadRequestException("Invalid ID format")
                val from: String? = call.request.queryParameters["from"]
                val to: String? = call.request.queryParameters["to"]

                val fromLocalDate: LocalDate = if (from.isNullOrBlank()) {
                    LocalDate.historyStart()
                } else {
                    LocalDate.parse(from)
                }
                val toLocalDate: LocalDate = if (to.isNullOrBlank()) {
                    LocalDate.startOfCurrentYear().minus(1, DateTimeUnit.DAY)
                } else {
                    LocalDate.parse(to)
                }

                // Missing / unknown client → zero balance (new client; gate may open)
                val clientFinanse = clientService.finanseForClient(id, fromLocalDate, toLocalDate)

                println("clientFinanse: $clientFinanse")

                call.respond(clientFinanse)

            } catch (e: Exception) {
                when (e) {
                    is BadRequestException -> throw e
                    else -> call.respondInternalError(e, "Failed to load client finances")
                }
            }
        }
    }
}

private fun requireCustomerClientId(call: ApplicationCall): Long {
    val principal = call.principal<JWTPrincipal>()
        ?: throw BadRequestException("Unauthorized")

    val jwtRole = principal.payload.getClaim("role").asString()
    if (jwtRole != "customer" && jwtRole != "admin") {
        throw BadRequestException("Forbidden")
    }

    return principal.payload.getClaim("userId").asString()
        .toLongOrNull()
        ?: throw BadRequestException("Invalid client id")
}
