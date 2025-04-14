package com.kontenery.controller

import com.kontenery.model.Contract
import com.kontenery.service.ContractService
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

fun Route.contractRoutes(service: ContractService) {
    route("/contract") {
        get {
            val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100
            println("findAll GETTER")
            call.respond(service.getAll(page, size))
        }
        get("{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val contract = service.getById(id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(contract)
        }
        post {
            try{
                println("POST CONTRACT")
                val contract = call.receive<Contract>()
                println(contract)
                val created = service.create(contract)
                call.respond(HttpStatusCode.Created, created)
            } catch(e:Exception) {
                println(e)
            }
        }
        put("{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@put call.respond(HttpStatusCode.BadRequest)
            val contract = call.receive<Contract>()
            val updated = service.update(id, contract)
            call.respond(updated)
        }
        delete("{id}") {
            val id = call.parameters["id"]?.toLongOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val deleted = service.delete(id)
            if (deleted) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound)
        }
    }
}
