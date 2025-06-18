package com.kontenery.controller

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.now
import com.kontenery.service.InvoiceService
import com.kontenery.service.PrintService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate

fun Route.invoiceRoutes(invoiceService: InvoiceService, printService: PrintService) {
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

                println("about to save invoice: ")
                val savedInvoice: Invoice? = invoiceService.createPeriodicInvoiceForClient(clientId, period, invoiceTitle)
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
            try {
                println("clientId: /sendInvoices/forAll")
                val periodRaw:String = call.queryParameters["period"].toString()

                var period: LocalDate? = null
                if(periodRaw.isNotBlank()) {
                    try {
                        period = LocalDate.parse(periodRaw)
                    } catch (e:Exception) {
                        println("period: $e")
                    }
                }

                val savedInvoices:List<Invoice> = invoiceService.createPeriodicInvoiceForAllClients(period)

                // TODO: Send Invoice to Clients

                call.respondNullable(savedInvoices)
            } catch (e:Exception) {
                println("post invoice: $e")
                call.respond(e.message.toString())
            }

        }
    }

}