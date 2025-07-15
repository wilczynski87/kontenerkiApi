package com.kontenery

import com.kontenery.controller.*
import com.kontenery.service.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    addressService: AddressService,
    clientService: ClientService,
    productService: ProductService,
    contractService: ContractService,
    invoiceService: InvoiceService,
    printService: PrintService,
    paymentService: PaymentService,
) {
    routing {

        get("/") {
            call.respondText("Hello World!")
        }
        get("health") {
            call.respond(HttpStatusCode.OK, "OK")
        }
        addressRouting(addressService)
        clientRoute(clientService)
        productRouting(productService)
        contractRoutes(contractService)
        invoiceRoutes(invoiceService, printService)
        mailSendConfirmation(invoiceService)
        paymentRoute(paymentService)
        CSVController()
    }
}
