package com.kontenery.controller

import com.kontenery.data.invoice.Invoice
import com.kontenery.ksef.dto.KsefInvoiceListResponse
import com.kontenery.ksef.dto.KsefLoginResponse
import com.kontenery.ksef.dto.KsefSendInvoiceResponse
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
                    ApiErrorResponse("KSeF login failed"),
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
                    ApiErrorResponse("KSeF invoice list failed"),
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
                    ApiErrorResponse("KSeF invoice send failed"),
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
                    ApiErrorResponse("KSeF invoice send failed"),
                )
            }
        }
    }
}
