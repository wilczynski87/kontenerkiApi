package com.kontenery.controller

import com.kontenery.model.invoice.Invoice
import com.kontenery.service.InvoiceService
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate

fun Route.invoiceRoutes(invoiceService: InvoiceService) {
    route("invoice") {
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
        // create SINGLE invoice for client
        post("/{clientId}") {
            try {
                var period: LocalDate? = null
                val clientId:Long = call.pathParameters["clientId"]?.toLongOrNull() ?: throw NullPointerException("There is no client Id")
                println("clientId: $clientId")
                val periodRaw:String = call.queryParameters["period"].toString()
                val invoiceTitle:String = call.queryParameters["invoiceTitle"].toString()
                if(periodRaw.isNotBlank()) {
                    try {
                        period = LocalDate.parse(periodRaw)
                    } catch (e:Exception) {
                        println("period: $e")
                    }
                }

                val savedInvoice:Invoice? = invoiceService.createPeriodicInvoiceForClient(clientId, period, invoiceTitle)

                // TODO: Send Invoice to Client

                call.respondNullable(savedInvoice)
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