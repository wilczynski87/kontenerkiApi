package com.kontenery.controller

import com.kontenery.data.Address
import com.kontenery.service.AddressService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.addressRouting(addressService: AddressService) {
    route("/address") {
        post {
            println("save ENDPOINT")
            val addressRecived: Address = call.receive<Address>()
            println("Otrzymałem: $addressRecived")
            val addresssaved: Address? = addressService.save(addressRecived)
            if(addresssaved == null) call.respond(HttpStatusCode.ExpectationFailed, addressRecived)
            else call.respond(HttpStatusCode.Created, addresssaved)
        }
        get("/findAll") {
            val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100
            println("findAll GETTER")
            val addresses = addressService.findAll(page, size)
            call.respond(addresses)
        }
        get("{id}") {
            println("get ID ENDPOINT")
            val id:Long? = call.request.pathVariables["id"]?.toLong()
            println("Otrzymałem id: $id\n")
            val address:Address? = addressService.findById(id!!)

            if(address == null) call.respond(HttpStatusCode.ExpectationFailed, "id: $id")
            else call.respond(address)
        }
    }
}