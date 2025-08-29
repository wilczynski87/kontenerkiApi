package com.kontenery.controller

import com.kontenery.library.model.Contract
import com.kontenery.library.model.ContractDto
import com.kontenery.service.ContractService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json

fun Route.contractRoutes(service: ContractService) {
    route("/contract") {
        get {
            val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100
            println("findAll GETTER")
            call.respond(service.getAll(page, size))
        }

        get("{id}") {
            println("GET CONTRACT BY ID")
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val contract = service.getById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(contract)
        }

        get("{id}/client") {
            println("GET CONTRACTS BY client Id")
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val contract = service.getByClientId(id)

            call.respond(contract)
        }

        get("{productId}/product") {
            println("Get Contract By Product")
            try {
                val id = call.parameters["productId"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest, "No product id")
                val contract: Contract? = service.getCurrentByProductId(id)
                println("Get Contract By Product: $contract")
                call.respondNullable(contract)

            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        post {
            try {
                println("POST CONTRACT")
                val contractDto:ContractDto = call.receive<ContractDto>()

                println("contractDto: $contractDto")
                val created: Contract = service.create(contractDto)
                println("created Contract: $created")
                call.respond(HttpStatusCode.Created, created)
            } catch(e:Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        put("{id}") {
            println("PUT Contract")
            try {
                val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
                val contract = call.receive<ContractDto>()
                val updated = service.update(id, contract)
                call.respond(updated)
            } catch(e:Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val deleted = service.delete(id)
            if (deleted) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound)
        }
    }
}
