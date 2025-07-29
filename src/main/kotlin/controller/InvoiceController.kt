package com.kontenery.controller

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.now
import com.kontenery.library.utils.startOfCurrentYear
import com.kontenery.service.InvoiceService
import com.kontenery.service.PrintService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate

fun Route.invoiceRoutes(
    invoiceService: InvoiceService,
    printService: PrintService
) {
    route("/invoice") {

        get("/{invoiceId}/id") {
            try {
                val invoiceId:Long = call.pathParameters["invoiceId"]?.toLongOrNull()
                    ?: throw NullPointerException("There is no valid invoiceId")

                val invoice:Invoice? = invoiceService.getInvoiceById(invoiceId)
                call.respondNullable(invoice)
            } catch (e:Exception) {
                println("/{invoiceId}/id Error: $e")
                call.respond(e.message.toString())
            }

        }

        get("/forDate") {

        }

        get("/{clientId}/forClient") {
            try {
                val clientId: Long = call.pathParameters["clientId"]?.toLongOrNull()
                    ?: throw NullPointerException("Brak Id klienta")
                val from: LocalDate = call.queryParameters["from"]?.let { LocalDate.parse(it) } ?: LocalDate.startOfCurrentYear()
                val to: LocalDate = call.queryParameters["to"]?.let { LocalDate.parse(it) } ?: LocalDate.now()
                println("/{clientId}/forClient: clientId: $clientId, from: $from, to: $to")

                val invoices: List<Invoice> = invoiceService.getInvoicesForClient(
                    clientId = clientId,
                    from = from,
                    to = to
                )
                println("invoices: $invoices")

                call.respond(invoices)

            } catch (e: Exception) {
                println(e)
                call.respond(e)
            }
        }
        // create SINGLE PERIODIC invoice for client
        post("/{clientId}") {
            // TODO logger!
//            println("create periodic invoice:")
            try {
                val clientId:Long = call.pathParameters["clientId"]?.toLongOrNull() ?: throw NullPointerException("There is no client Id")
                var period:LocalDate? = null
//                println("clientId: $clientId")
                val periodRaw:String = call.queryParameters["period"].toString()
//                println("periodRaw: $periodRaw")
                val invoiceTitle:String = call.queryParameters["invoiceTitle"].toString()
//                println("invoiceTitle: $invoiceTitle")
                if(periodRaw.isNotBlank() && periodRaw != "null") {
                    try {
                        period = LocalDate.parse(periodRaw)
                    } catch (e:Exception) {
                        println("period: $e")
                    }
                } else period = LocalDate.now()

//                println("period: $period")
//                println("about to save invoice: ")
                val savedInvoice: Invoice? = invoiceService.createPeriodicInvoiceForClient(clientId, period!!, invoiceTitle)
                println("savedInvoice: $savedInvoice")

                // TODO: Send Invoice to Client
                if (savedInvoice != null) {
                    printService.sendPeriodicInvoice(savedInvoice)
                }
                println("Mail przyjęty, od clientId: $clientId")

                call.respond(mapOf("invoiceNumber" to (savedInvoice?.invoiceNumber
                    ?: throw NullPointerException("No invoice number for savedInvoice"))))
//                call.respond(savedInvoice?.invoiceNumber ?: throw NullPointerException("No invoice number for savedInvoice"))
            } catch (e:Exception) {
                println("post invoice: $e")
                call.respond(e.message.toString())
            }

        }
        post("/sendInvoices/forAll") {
            println("/sendInvoices/forAll recived")
            try {
                println("clientId: /sendInvoices/forAll")
                val periodRaw:String = call.queryParameters["period"].toString()

//                var period: LocalDate? = null
                val period: LocalDate = if(periodRaw.isNotBlank() || periodRaw.toLowerCasePreservingASCIIRules().trim() != "null") {
                    try {
                        LocalDate.parse(periodRaw)
                    } catch (e:Exception) {
                        println("period: $e")
                        throw NullPointerException("/sendInvoices/forAll can not parse date: $periodRaw")
                    }
                } else LocalDate.now()

                val savedInvoices:List<Invoice> = invoiceService.createPeriodicInvoiceForAllClients(period)

                // TODO: Send Invoice to Clients
                savedInvoices.forEach { savedInvoice ->
                    printService.sendPeriodicInvoice(savedInvoice)
                }

                call.respondNullable(savedInvoices)
            } catch (e:Exception) {
                println("post invoice: $e")
                call.respond(e.message.toString())
            }
        }
        post("/{customerId}/custom") {
            println("Custom Invoice create")
            try {
                val customerId: Long = call.pathParameters["customerId"]?.toLong() ?: throw TypeCastException("customer Id is wrong /{customerId}/custom")
                val invoice: Invoice = call.receive<Invoice>()

                println("invoice String: $invoice")

                val savedInvoice:Invoice? = invoiceService.createCustomInvoice(invoice)

                if(savedInvoice == null) {
                    call.respond(HttpStatusCode.ExpectationFailed, "Could not save invoice")
                } else {
                    // Send Invoice to Clients
                    printService.sendPeriodicInvoice(savedInvoice)
                    // Respond to front
                    call.respondNullable(savedInvoice)
                }
            } catch (e:Exception) {
                println("post invoice error: $e")
                call.respond(e.message.toString())
            }
        }
    }

}