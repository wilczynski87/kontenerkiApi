package com.kontenery.controller

import com.kontenery.model.Address
import com.kontenery.model.Client
import com.kontenery.service.ClientService
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.util.reflect.*

fun Route.clientRoute(clientService: ClientService) {
    route("/client") {

        post {
            val client: Client = call.receive<Client>()
            val saveClient:Client? = clientService.save(client)
            if(saveClient != null) call.respond(saveClient)
            else call.respond(HttpStatusCode.ExpectationFailed)
        }

        get("/findAll") {
            val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100
            println("findAll GETTER")
            val clients: List<Client> = clientService.getAllClients(page, size)
            call.respond(clients)
        }

        get("/{id}") {
            println("get ID ENDPOINT")
            val id:Long? = call.request.pathVariables["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID format")
            println("Otrzymałem id: $id\n")
            val client: Client? = clientService.findClientById(id!!)

            if(client == null) call.respond(HttpStatusCode.ExpectationFailed, "id: $id")
            else call.respond(client)
        }

        put("{id}") {
            val id = call.pathParameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID format")

            val clientUpdate = call.receive<Client>()
            val updatedClient = clientService.updateClient(clientUpdate)
                ?: throw NotFoundException("Client not found")

            call.respond(updatedClient)
        }
    }
}