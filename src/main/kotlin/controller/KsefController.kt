package com.kontenery.controller

import com.kontenery.data.invoice.Invoice
import com.kontenery.ksef.dto.KsefDownloadInvoiceResponse
import com.kontenery.ksef.dto.KsefDownloadInvoicesMonthResponse
import com.kontenery.ksef.dto.KsefInvoiceListResponse
import com.kontenery.ksef.dto.KsefInvoiceRegisteredResponse
import com.kontenery.ksef.dto.KsefLoginResponse
import com.kontenery.ksef.dto.KsefSendInvoiceResponse
import com.kontenery.ksef.exception.KsefErrorMessages
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.service.KsefService
import com.kontenery.utils.ApiErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("KsefController")

fun Route.ksefRoutes(ksefService: KsefService) {
    route("/ksef") {
        get("/login") {
            try {
                val response: KsefLoginResponse = ksefService.login()
                call.respond(HttpStatusCode.OK, response)
            } catch (e: KsefException) {
                logger.error("KSeF login failed", e)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    ApiErrorResponse(KsefErrorMessages.userMessage(e)),
                )
            }
        }

        get("/invoices") {
            try {
                val pageOffset = call.request.queryParameters["pageOffset"]?.toIntOrNull() ?: 0
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 50
                val from = call.request.queryParameters["from"]
                val to = call.request.queryParameters["to"]
                val subjectType = call.request.queryParameters["subjectType"] ?: "Subject1"

                val response: KsefInvoiceListResponse = ksefService.listInvoicesKsef(
                    from = from,
                    to = to,
                    pageOffset = pageOffset,
                    pageSize = pageSize,
                    subjectType = subjectType,
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("Invalid request"))
            } catch (e: KsefException) {
                logger.error("KSeF invoice list failed", e)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    ApiErrorResponse(KsefErrorMessages.userMessage(e)),
                )
            }
        }

        get("/invoices/registered") {
            try {
                val invoiceNumber = call.request.queryParameters["invoiceNumber"]
                if (invoiceNumber.isNullOrBlank()) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiErrorResponse("invoiceNumber query parameter is required"),
                    )
                }
                val subjectType = call.request.queryParameters["subjectType"] ?: "Subject1"
                val response: KsefInvoiceRegisteredResponse =
                    ksefService.isInvoiceRegisteredInKsef(invoiceNumber, subjectType)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(e.message ?: "Invalid request"))
            } catch (e: KsefException) {
                logger.error("KSeF invoice registration check failed", e)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    ApiErrorResponse(KsefErrorMessages.userMessage(e)),
                )
            }
        }

        get("/invoices/download") {
            try {
                val year = call.request.queryParameters["year"]?.toIntOrNull()
                val month = call.request.queryParameters["month"]?.toIntOrNull()
                val subjectType = call.request.queryParameters["subjectType"] ?: "Subject1"

                if (year != null || month != null) {
                    if (year == null || month == null) {
                        return@get call.respond(
                            HttpStatusCode.BadRequest,
                            ApiErrorResponse("Both year and month are required for monthly download"),
                        )
                    }
                    val response: KsefDownloadInvoicesMonthResponse =
                        ksefService.downloadInvoicesForMonthFromKsef(year, month, subjectType)
                    return@get call.respond(HttpStatusCode.OK, response)
                }

                val ksefNumber = call.request.queryParameters["ksefNumber"]
                val invoiceNumber = call.request.queryParameters["invoiceNumber"]
                if (ksefNumber.isNullOrBlank() && invoiceNumber.isNullOrBlank()) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ApiErrorResponse("Provide ksefNumber, invoiceNumber, or year+month"),
                    )
                }

                val response: KsefDownloadInvoiceResponse = ksefService.downloadInvoiceFromKsef(
                    ksefNumber = ksefNumber,
                    invoiceNumber = invoiceNumber,
                    subjectType = subjectType,
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(e.message ?: "Invalid request"))
            } catch (e: KsefException) {
                logger.error("KSeF invoice download failed", e)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    ApiErrorResponse(KsefErrorMessages.userMessage(e)),
                )
            }
        }

        post("/invoices/{invoiceNumber}/send") {
            try {
                val invoiceNumber: String? = call.parameters["invoiceNumber"]
                if(invoiceNumber.isNullOrBlank()) return@post call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("Invalid invoiceNumber"))

                val response: KsefSendInvoiceResponse = ksefService.sendInvoiceToKsefByNumber(invoiceNumber)
                call.respond(HttpStatusCode.Accepted, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("Invalid invoice"))
            } catch (e: KsefException) {
                logger.error("KSeF invoice send by number failed", e)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    ApiErrorResponse(KsefErrorMessages.userMessage(e)),
                )
            }
        }

        post("/invoices/send") {
            try {
                val invoice: Invoice = call.receive()
                val response: KsefSendInvoiceResponse = ksefService.sendInvoiceToKsef(invoice)
                call.respond(HttpStatusCode.Accepted, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("Invalid invoice"))
            } catch (e: KsefException) {
                logger.error("KSeF invoice send failed", e)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    ApiErrorResponse(KsefErrorMessages.userMessage(e)),
                )
            }
        }
    }
}
