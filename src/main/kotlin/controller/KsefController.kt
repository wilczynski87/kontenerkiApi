package com.kontenery.controller

import com.kontenery.data.invoice.Invoice
import com.kontenery.ksef.dto.KsefInvoiceListResponse
import com.kontenery.ksef.dto.KsefLoginResponse
import com.kontenery.ksef.dto.KsefSendInvoiceResponse
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.service.KsefService
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
                    mapOf("error" to (e.message ?: "KSeF login failed")),
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

                val response: KsefInvoiceListResponse = ksefService.listInvoices(
                    from = from,
                    to = to,
                    pageOffset = pageOffset,
                    pageSize = pageSize,
                    subjectType = subjectType,
                )
                call.respond(HttpStatusCode.OK, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request")))
            } catch (e: KsefException) {
                logger.error("KSeF invoice list failed", e)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    mapOf("error" to (e.message ?: "KSeF invoice list failed")),
                )
            }
        }

        post("/invoices/{invoiceId}/send") {
            try {
                val invoiceId = call.parameters["invoiceId"]?.toLongOrNull()
                    ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid invoiceId"))

                val response: KsefSendInvoiceResponse = ksefService.sendInvoiceById(invoiceId)
                call.respond(HttpStatusCode.Accepted, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid invoice")))
            } catch (e: KsefException) {
                logger.error("KSeF invoice send by id failed", e)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    mapOf("error" to (e.message ?: "KSeF invoice send failed")),
                )
            }
        }

        post("/invoices/send") {
            try {
                val invoice: Invoice = call.receive()
                val response: KsefSendInvoiceResponse = ksefService.sendInvoice(invoice)
                call.respond(HttpStatusCode.Accepted, response)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid invoice")))
            } catch (e: KsefException) {
                logger.error("KSeF invoice send failed", e)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    mapOf("error" to (e.message ?: "KSeF invoice send failed")),
                )
            }
        }
    }
}
