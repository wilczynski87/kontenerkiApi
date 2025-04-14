package com.kontenery

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kontenery.controller.addressRouting
import com.kontenery.controller.clientRoute
import com.kontenery.controller.contractRoutes
import com.kontenery.controller.productRouting
import com.kontenery.service.AddressService
import com.kontenery.service.ClientService
import com.kontenery.service.ContractService
import com.kontenery.service.ProductService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection
import java.sql.DriverManager
import org.jetbrains.exposed.sql.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureRouting(
    addressService: AddressService,
    clientService: ClientService,
    productService: ProductService,
    contractService: ContractService
) {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("health") {
            call.respond(HttpStatusCode.OK, "")
        }
        addressRouting(addressService)
        clientRoute(clientService)
        productRouting(productService)
        contractRoutes(contractService)
    }
}
