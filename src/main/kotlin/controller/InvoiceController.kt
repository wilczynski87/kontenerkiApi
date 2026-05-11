package com.kontenery.controller

import com.kontenery.data.Client
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.utils.endOfCurrentMonth
import com.kontenery.data.utils.errors.ErrorMessage
import com.kontenery.data.utils.now
import com.kontenery.data.utils.startOfCurrentMonth
import com.kontenery.data.utils.startOfCurrentYear
import com.kontenery.service.ClientService
import com.kontenery.service.InvoiceService
import com.kontenery.service.PrintService
import com.kontenery.utils.cookRawPeriod
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
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

        get("/{invoiceNumber}/number") {
            try {
                val invoiceNumber:String = call.pathParameters["invoiceNumber"]
                    ?: throw NullPointerException("There is no valid invoiceNumber")

                val invoice:Invoice? = invoiceService.getInvoiceByNumber(invoiceNumber)
                println("invoiceNumber: $invoice")

                call.respond(invoice ?: "brak")
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

                val invoices: List<Invoice> = invoiceService.getInvoicesAndBillsForClient(
                    clientId = clientId,
                    from = from,
                    to = to
                )
                println("Numery faktur/rachunków utworzonych:")
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

                if(errorList.isEmpty()) call.respond(savedInvoice.invoiceNumber as Any)
                else call.respond(HttpStatusCode.ExpectationFailed, errorList)
//                call.respond(savedInvoice?.invoiceNumber ?: throw NullPointerException("No invoice number for savedInvoice"))
            } catch (e: IllegalStateException) {
                println("$e, errorList: $errorList")
                call.respond(HttpStatusCode.ExpectationFailed, errorList)
            } catch (e:Exception) {
                println("post invoice: $e")
                call.respond(HttpStatusCode.ExpectationFailed, e)
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

//                val savedInvoices:List<Invoice> = createdInvoice.mapNotNull { invoiceService.saveInvoice(it) }
                val savedInvoices:List<Invoice> = createdInvoice.mapNotNull { invoiceService.saveInvoiceWithErrors(it.vatApply, it, errorList) }

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
            println("Custom Invoice creation:")
            try {
                val customerId: Long = call.pathParameters["customerId"]?.toLongOrNull() ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invalid customerId")
                )
                val client = clientService.findClientById(customerId) ?: return@post call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Client not found with id $customerId")
                )

                val invoice: Invoice = try {
                    call.receive()
                } catch (e: Exception) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid invoice body: ${e.message}")
                    )
                }

                val updatedInvoice: Invoice = invoice.copy(
                    customer = invoice.customer?.copy(
                        client = client
                    )
                )

                val savedInvoice:Invoice? = invoiceService.createCustomInvoice(updatedInvoice)
                println("invoice saved: $savedInvoice")

                if(savedInvoice == null) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Could not save invoice")
                    )
                } else {
//                    println("invoice is not null: $savedInvoice")
                    // Send Invoice to Clients
                    printService.sendPeriodicInvoice(savedInvoice)
                    // Respond to front
                    call.respond(savedInvoice)
                }
            } catch (e:Exception) {
                println("post invoice error: $e")
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to e.message)
                )
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

                call.respond(true)

            } catch (e: Exception) {
                println("/{month}/print: ${e.message}")
            }
        }

        post("/sendAgain") {
            try {
                val invoiceNumber: String = call.receive()

                println("invoiceNumber: $invoiceNumber")

                val invoice: Invoice = invoiceService.getInvoiceByNumber(invoiceNumber)
                    ?: throw IllegalStateException("Can not find Invoice with given ID: $invoiceNumber")
//                println("sendInvoiceAgain: $invoice")

                val invoiceSend = printService.sendInvoiceAgain(invoice)

                call.respond(invoiceSend)
            } catch (e: Exception) {
                println("/{invoiceId}/sendAgain: ${e.message}")
                call.respond(HttpStatusCode.Conflict,"${e.message}", )
            }
        }
    }

}