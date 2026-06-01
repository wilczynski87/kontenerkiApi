package com.kontenery.controller

import com.kontenery.data.Client
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.InvoiceSend
import com.kontenery.data.utils.endOfCurrentMonth
import com.kontenery.data.utils.errors.ErrorMessage
import com.kontenery.data.utils.errors.InvoiceErrorMessage
import com.kontenery.data.utils.now
import com.kontenery.data.utils.startOfCurrentMonth
import com.kontenery.data.utils.startOfCurrentYear
import com.kontenery.ksef.exception.KsefErrorMessages
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.service.KsefService
import com.kontenery.service.ClientService
import com.kontenery.service.InvoiceService
import com.kontenery.service.PrintService
import com.kontenery.utils.ApiErrorResponse
import com.kontenery.utils.cookRawPeriod
import com.kontenery.utils.respondInternalError
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate

fun Route.invoiceRoutes(
    invoiceService: InvoiceService,
    printService: PrintService,
    clientService: ClientService,
    ksefService: KsefService,
) {
    route("/invoice") {

        get("/{invoiceNumber}/id") {
            try {
                val invoiceNumber:String = call.pathParameters["invoiceNumber"]
                    ?: throw NullPointerException("There is no valid invoiceId")

                val invoice:Invoice? = invoiceService.getInvoiceByNumber(invoiceNumber)
                call.respondNullable(invoice)
            } catch (e:Exception) {
                call.respondInternalError(e, "Failed to load invoice")
            }

        }

        get("/{invoiceNumber}/number") {
            try {
                val invoiceNumber:String = call.pathParameters["invoiceNumber"]
                    ?: throw NullPointerException("There is no valid invoiceNumber")

                val invoice:Invoice? = invoiceService.getInvoiceByNumber(invoiceNumber)
                call.respond(invoice ?: "brak")
            } catch (e:Exception) {
                call.respondInternalError(e, "Failed to load invoice")
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
                val invoices: List<Invoice> = invoiceService.getInvoicesAndBillsForClient(
                    clientId = clientId,
                    from = from,
                    to = to
                )

                call.respond(invoices)

            } catch (e: Exception) {
                call.respondInternalError(e, "Failed to load invoices for client")
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

                val createdInvoice: Invoice = invoiceService.createPeriodicInvoiceForClient(client, period, errorList)
                    ?: throw IllegalStateException("Could not create invoice for client: $clientId")
                println("created Invoice/Bill: $createdInvoice")

                val savedInvoice: Invoice = saveInvoiceWithOptionalKsef(
                    createdInvoice,
                    invoiceService,
                    ksefService,
                    errorList,
                ) ?: throw IllegalStateException("Could not create or save invoice for client: $clientId")

                printService.sendPeriodicInvoice(savedInvoice)
                println("Mail wysłany, od clientId: $clientId")

                if(errorList.isEmpty()) call.respond(savedInvoice.invoiceNumber as Any)
                else call.respond(HttpStatusCode.ExpectationFailed, errorList)
//                call.respond(savedInvoice?.invoiceNumber ?: throw NullPointerException("No invoice number for savedInvoice"))
            } catch (e: IllegalStateException) {
                println("$e, errorList: $errorList")
                call.respond(HttpStatusCode.ExpectationFailed, errorList)
            } catch (e:Exception) {
                if (errorList.isNotEmpty()) {
                    call.respond(HttpStatusCode.ExpectationFailed, errorList)
                } else {
                    call.respondInternalError(e, "Failed to create invoice")
                }
            }
        }
        post("/sendInvoices/forAll") {
            val errorList: MutableList<ErrorMessage> = mutableListOf()
            try {
                val periodRaw:String = call.queryParameters["period"].toString()

                val period: LocalDate = cookRawPeriod(periodRaw, "/sendInvoices/forAll")

                val allClients: List<Client> = clientService.getFilteredClients(true)

                val createdInvoice:List<Invoice> =
                    allClients.mapNotNull { invoiceService.createPeriodicInvoiceForClient(it, period, errorList) }

                val savedInvoices: List<Invoice> = createdInvoice.mapNotNull { invoice ->
                    saveInvoiceWithOptionalKsef(invoice, invoiceService, ksefService, errorList)
                }

                savedInvoices.filter { it.vatApply.not() }
                    .forEach { savedInvoice ->
                        printService.sendPeriodicInvoice(savedInvoice)
                    }

                call.respond(errorList)
            } catch (e:Exception) {
                if (errorList.isNotEmpty()) {
                    call.respond(HttpStatusCode.ExpectationFailed, errorList)
                } else {
                    call.respondInternalError(e, "Failed to send invoices")
                }
            }
        }

        post("/{customerId}/custom") {
            println("Custom Invoice creation:")
            try {
                val customerId: Long =
                    call.pathParameters["customerId"]?.toLongOrNull() ?: return@post call.respond(
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
                        ApiErrorResponse("Invalid invoice body, $e"),
                    )
                }

                val updatedInvoice: Invoice = invoice.copy(
                    customer = invoice.customer?.copy(
                        client = client
                    )
                )

                val createdInvoice: Invoice = invoiceService.createCustomInvoice(updatedInvoice)
                    ?: throw NoSuchElementException("Could not created Invoice/Bill")
                println("createdInvoice: $createdInvoice")

                val savedInvoice: Invoice = saveInvoiceWithOptionalKsef(
                    createdInvoice,
                    invoiceService,
                    ksefService,
                ) ?: throw NoSuchElementException("Could not save Invoice/Bill")

                if(savedInvoice.vatApply.not()) printService.sendPeriodicInvoice(savedInvoice)
                else {
                    if(savedInvoice.invoiceNumber.isNullOrBlank()) throw NullPointerException("Brak invoice number")
                    val isInvoiceInKsef = ksefService.isInvoiceRegisteredInKsef(savedInvoice.invoiceNumber)
                    println("isInvoiceInKsef: $isInvoiceInKsef")
                }

                // Respond to front
                call.respond(savedInvoice)

            } catch (e: KsefException) {
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    ApiErrorResponse(KsefErrorMessages.userMessage(e)),
                )
            } catch (e: NoSuchElementException) {
                println("post invoice error: $e")
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Could not save invoice")
                )
            } catch (e:Exception) {
                call.respondInternalError(e, "Failed to create custom invoice")
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

                val sendInvoice = saveInvoiceWithOptionalKsef(invoice, invoiceService, ksefService, mutableListOf<ErrorMessage>())

                val invoiceSend = if(sendInvoice?.vatApply ?: false) printService.sendInvoiceAgain(invoice)
                else InvoiceSend(
                    sendInvoice?.invoiceNumber,
                    sendInvoice?.customer?.name,
                    invoice.invoiceDate,
                    LocalDate.now()
                )

                call.respond(invoiceSend)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, ApiErrorResponse("Failed to resend invoice: $e"))
            }
        }
    }

}