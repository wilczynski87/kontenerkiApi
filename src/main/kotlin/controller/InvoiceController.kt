package com.kontenery.controller

import com.kontenery.library.model.Client
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.endOfCurrentMonth
import com.kontenery.library.utils.errors.ErrorMessage
import com.kontenery.library.utils.now
import com.kontenery.library.utils.startOfCurrentMonth
import com.kontenery.library.utils.startOfCurrentYear
import com.kontenery.service.ClientService
import com.kontenery.service.InvoiceService
import com.kontenery.service.PrintService
import com.kontenery.utils.cookRawPeriod
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import kotlinx.serialization.json.Json

fun Route.invoiceRoutes(
    invoiceService: InvoiceService,
    printService: PrintService,
    clientService: ClientService,
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
                println("invoices:")
                invoices.forEach { println(it) }

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
            val errorList: MutableList<ErrorMessage> = mutableListOf()
            try {
                val clientId:Long = call.pathParameters["clientId"]?.toLongOrNull() ?: throw NullPointerException("There is no client Id")
                var period:LocalDate? = null
//                println("clientId: $clientId")
                val periodRaw:String = call.queryParameters["period"].toString()
//                println("periodRaw: $periodRaw")

                if(periodRaw.isNotBlank() && periodRaw != "null") {
                    try {
                        period = LocalDate.parse(periodRaw)
                    } catch (e:Exception) {
                        println("period: $e")
                    }
                } else period = LocalDate.now()

                val client: Client = clientService.findClientById(clientId)
                    ?: throw NullPointerException("no Client with given Id: $clientId")

                val createInvoice: Invoice = invoiceService.createPeriodicInvoiceForClient(client, period, errorList)
                    ?: throw IllegalStateException("Could not create invoice for client: $clientId")
                println("savedInvoice: $createInvoice")

                val savedInvoice: Invoice = invoiceService.saveInvoiceWithErrors(client.needInvoice(), createInvoice, errorList)
                    ?: throw IllegalStateException("Could not create or save invoice for client: $clientId")

                // TODO: Send Invoice to Client
                printService.sendPeriodicInvoice(savedInvoice)
                println("Mail wysłany, od clientId: $clientId")

                call.respond(errorList)
//                call.respond(savedInvoice?.invoiceNumber ?: throw NullPointerException("No invoice number for savedInvoice"))
            } catch (e: IllegalStateException) {
                println("$e, errorList: $errorList")
                call.respond(errorList)
            } catch (e:Exception) {
                println("post invoice: $e")
                call.respond(e)
            }
        }
        post("/sendInvoices/forAll") {
            try {
                println("clientId: /sendInvoices/forAll")
                val periodRaw:String = call.queryParameters["period"].toString()
//                println("periodRaw: $periodRaw")

                val period: LocalDate = cookRawPeriod(periodRaw, "/sendInvoices/forAll")

                val allClients: List<Client> = clientService.getFilteredClients(true)
                println("clients: ${allClients.map{ it.getName()}}")

                // create Validator
                val errorList: MutableList<ErrorMessage> = mutableListOf()

                val createdInvoice:List<Invoice> =
                    allClients.mapNotNull { invoiceService.createPeriodicInvoiceForClient(it, period, errorList) }
                println("invoices: ${createdInvoice.map{ it.invoiceNumber}}")

                val savedInvoices:List<Invoice> = createdInvoice.mapNotNull { invoiceService.saveInvoice(it) }

                savedInvoices.forEach { savedInvoice ->
                    println("document send: vat apply - ${savedInvoice.vatApply}, numer - ${savedInvoice.invoiceNumber}")
                    printService.sendPeriodicInvoice(savedInvoice)
                }

                call.respond(errorList)
            } catch (e:Exception) {
                println("post invoice: $e")
                call.respond(e.message.toString())
            }
        }

        post("/{customerId}/custom") {
            println("Custom Invoice create")
            try {
                val customerId: Long = call.pathParameters["customerId"]?.toLong() ?: throw TypeCastException("customer Id is wrong /{customerId}/custom")
                val client = clientService.findClientById(customerId) ?: throw NullPointerException("There is no such Client, with given Id: $customerId")
                val invoice: Invoice = call.receive<Invoice>()
                println("invoice String: $invoice")

                val updatedInvoice: Invoice = invoice.copy(
                    customer = invoice.customer?.copy(
                        client = client
                    )
                )

                val savedInvoice:Invoice? = invoiceService.createCustomInvoice(updatedInvoice)
                println("invoice saved: $savedInvoice")

                if(savedInvoice == null) {
                    call.respond(HttpStatusCode.ExpectationFailed, "Could not save invoice")
                } else {
//                    println("invoice is not null: $savedInvoice")
                    // Send Invoice to Clients
                    printService.sendPeriodicInvoice(savedInvoice)
                    // Respond to front
                    call.respond(savedInvoice)
                }
            } catch (e:Exception) {
                println("post invoice error: $e")
                call.respond(e.message.toString())
            }
        }

        get("/{date}/print") {
            try {
                val month: LocalDate = call.pathParameters["date"]?.let { LocalDate.parse(it) } ?: throw TypeCastException("month can not be read /{date}/print")
                println("month: $month")

                val invoices: List<Invoice> = invoiceService.getInvoicesForDate(
                    0,
                    1000,
                    LocalDate.startOfCurrentMonth(month),
                    LocalDate.endOfCurrentMonth(month)
                )

                printService.printInvoices(invoices)

                call.respond("Invoices send")

            } catch (e: Exception) {
                println("/{month}/print: ${e.message}")
            }
        }

        get("/{invoiceNumber}/sendAgain") {
            try {
                val invoiceNumber: String = call.pathParameters["invoiceNumber"]
                    ?: throw TypeCastException("can not read invoice ID {invoiceId}/sendAgain")

                println("invoiceNumber: $invoiceNumber")

                val invoice: Invoice = invoiceService.getInvoiceByNumber(invoiceNumber)
                    ?: throw IllegalStateException("Can not find Invoice with given ID: $invoiceNumber")

                printService.sendInvoiceAgain(invoice)

                call.respond(Json.encodeToString("Invoice send"))
            } catch (e: Exception) {
                println("/{invoiceId}/sendAgain: ${e.message}")
                call.respond(HttpStatusCode.Conflict,"${e.message}", )
            }
        }
    }

}