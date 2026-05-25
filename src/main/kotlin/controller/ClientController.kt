package com.kontenery.controller

import com.kontenery.data.Client
import com.kontenery.data.finance.ClientFinanceDto
import com.kontenery.data.utils.startOfCurrentYear
import com.kontenery.service.ClientService
import com.kontenery.utils.ApiErrorResponse
import com.kontenery.utils.respondInternalError
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
fun Route.clientRoute(clientService: ClientService) {
    route("/client") {

        post {
            try {
                val client: Client = call.receive<Client>()
                val saveClient: Client? = clientService.save(client)
                if (saveClient != null) call.respond(saveClient)
                else call.respond(HttpStatusCode.ExpectationFailed, ApiErrorResponse("Failed to save client"))
            } catch (e: Exception) {
                call.respondInternalError(e, "Failed to save client")
            }
        }

        get("/findAll") {
            val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100
            val clients: List<Client> = clientService.getAllClients(page, size)
            call.respond(clients)
        }

        get("/{id}/id") {
            val id: Long = call.request.pathVariables["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID format")
            val client: Client? = clientService.findClientById(id)

            if (client == null) {
                call.respond(HttpStatusCode.NotFound, ApiErrorResponse("Client not found"))
            } else {
                call.respond(client)
            }
        }

        put("/{id}") {
            try {
                call.pathParameters["id"]?.toLongOrNull()
                    ?: throw BadRequestException("Invalid ID format")

                val clientUpdate = call.receive<Client>()
                val updatedClient = clientService.updateClient(clientUpdate)
                    ?: throw NotFoundException("Client not found")

                call.respond(updatedClient)
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
                    LocalDate.startOfCurrentYear().minus(1, DateTimeUnit.YEAR)
                } else {
                    LocalDate.parse(from)
                }
                val toLocalDate: LocalDate = if (to.isNullOrBlank()) {
                    LocalDate.startOfCurrentYear().minus(1, DateTimeUnit.DAY)
                } else {
                    LocalDate.parse(to)
                }

                val clientFinanse: ClientFinanceDto = clientService.finanseForClient(id, fromLocalDate, toLocalDate)
                    ?: throw NotFoundException("ClientFinance not found")

                call.respond(clientFinanse)

            } catch (e: Exception) {
                when (e) {
                    is BadRequestException, is NotFoundException -> throw e
                    else -> call.respondInternalError(e, "Failed to load client finances")
                }
            }
        }
    }
}
