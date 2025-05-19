package com.kontenery.controller

import com.kontenery.model.Client
import com.kontenery.service.ClientService
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

fun Route.clientRoute(clientService: ClientService) {
    route("/client") {

        get("/list") {
            val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100

            val clientList = clientService.getClientList(page, size)

            call.respond(clientList)
        }

        post {
            println("zaczynamy SAVE klienta: ")
            try {
                val client: Client = call.receive<Client>()
                println(client)
                val saveClient:Client? = clientService.save(client)
                println("zapisany: $saveClient")
                if(saveClient != null) call.respond(saveClient)
                else call.respond(HttpStatusCode.ExpectationFailed)
            } catch (e:Exception) {
                println(e)
            }
        }

        get("/findAll") {
            val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100
            println("findAll GETTER")
            val clients: List<Client> = clientService.getAllClients(page, size)
            call.respond(clients)
        }

        get("/{id}/id") {
            println("get ID ENDPOINT")
            val id:Long? = call.request.pathVariables["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID format")
            println("Otrzymałem id: $id\n")
            val client: Client? = clientService.findClientById(id!!)
            println("Klient: $client\n")

            if(client == null) call.respond(HttpStatusCode.ExpectationFailed, "Brak klienta o id: $id")
            else call.respond(client)
        }

        put("/{id}") {
            println("zaczynamy PUT client: ")
            val id = call.pathParameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID format")

            val clientUpdate = call.receive<Client>()
            println(clientUpdate)
            val updatedClient = clientService.updateClient(clientUpdate)
                ?: throw NotFoundException("Client not found")

            call.respond(updatedClient)
        }
    }
}