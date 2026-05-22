package com.kontenery

import com.kontenery.controller.*
import com.kontenery.service.*
import com.kontenery.validator.BankAccountValidator
import com.kontenery.validator.PaymentValidator
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
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
    csvService: CSVService,
    bankAccountService: BankAccountService,
    listingService: ListingService,
    utilitiesService: UtilitiesService,
    authService: AuthService,
    ksefService: com.kontenery.ksef.service.KsefService,
    paymentValidator: PaymentValidator,
    bankAccountValidator: BankAccountValidator,
) {
    routing {

        get("/") {
            call.respondText("Hello World!")
        }
        get("health") {
            call.respond(HttpStatusCode.OK, "OK")
        }
        authenticate("auth-jwt") {
            get("securityTest") {
                val principal = call.principal<JWTPrincipal>()
                val role = principal!!.getClaim("role", String::class)
                call.respondText("Hello $role")
            }
            addressRouting(addressService)
            clientRoute(clientService)
            productRouting(productService)
            contractRoutes(contractService, clientService, productService)
            invoiceRoutes(invoiceService, printService, clientService, ksefService)
            paymentRoute(paymentService)
            CSVController(csvService, paymentService, paymentValidator)
            bankAccountController(bankAccountService, bankAccountValidator)
            listingRoute(listingService)
            utilitiesController(utilitiesService, clientService)
            ksefRoutes(ksefService)
        }
        mailSendConfirmation(invoiceService)
        authController(authService)
        bramaController()
    }
}
