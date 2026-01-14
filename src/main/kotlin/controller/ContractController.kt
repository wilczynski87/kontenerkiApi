package com.kontenery.controller

import com.kontenery.library.model.Client
import com.kontenery.library.model.Contract
import com.kontenery.library.model.ContractDto
import com.kontenery.library.model.Product
import com.kontenery.service.ClientService
import com.kontenery.service.ContractService
import com.kontenery.service.ProductService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import java.lang.NullPointerException

fun Route.contractRoutes(
    service: ContractService,
    clientService: ClientService,
    productService: ProductService,
) {
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
            val contracts = service.getByClientId(id)

            call.respond(contracts)
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
            val productId: Long = service.getProductByContractId(id)?.id ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val productReleased: Boolean = productService.releaseProduct(productId)

            val deleted = service.delete(id)
            
            if (deleted) call.respond(HttpStatusCode.NoContent)
            else call.respond(HttpStatusCode.NotFound)
        }

        post("updateDB") {
            try {
                val contracts: List<Contract> = call.receive<List<Contract>>()
                println("contracts")
                println(contracts)

                val clients: List<Client> = contracts.mapNotNull { it.client }.mapNotNull { clientService.save(client = it) }

                val products: List<Product> = contracts.mapNotNull { it.product }.mapNotNull { productService.save(it) }

                contracts.forEach { service.save(it) }
                call.respond("OK")
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}
