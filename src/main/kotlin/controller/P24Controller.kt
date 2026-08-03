package com.kontenery.controller

import com.kontenery.p24.dto.P24CreateTransactionRequest
import com.kontenery.p24.dto.P24NotificationPayload
import com.kontenery.p24.exception.P24Exception
import com.kontenery.p24.service.P24Service
import com.kontenery.utils.ApiErrorResponse
import com.kontenery.utils.isValidInternalApiKey
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("P24Controller")

fun Route.p24Routes(p24Service: P24Service) {
    route("/p24") {
        post("/transactions") {
            if (!call.isValidInternalApiKey()) {
                call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("Unauthorized"))
                return@post
            }
            try {
                val request = call.receive<P24CreateTransactionRequest>()
                val response = p24Service.createTransaction(request)
                call.respond(HttpStatusCode.Created, response)
            } catch (e: P24Exception) {
                logger.warn("P24 create transaction failed: {}", e.message)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    ApiErrorResponse(e.message ?: "P24 error"),
                )
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(e.message ?: "Bad request"))
            }
        }

        get("/transactions/{sessionId}") {
            if (!call.isValidInternalApiKey()) {
                call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("Unauthorized"))
                return@get
            }
            try {
                val sessionId = call.parameters["sessionId"]
                    ?: throw P24Exception("sessionId is required", statusCode = 400)
                val response = p24Service.getTransaction(sessionId)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: P24Exception) {
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    ApiErrorResponse(e.message ?: "P24 error"),
                )
            }
        }

        /**
         * Public webhook (urlStatus). P24 expects HTTP 200 with body "OK" after successful processing.
         * Integrity is verified via CRC signature (skipped only in P24_MOCK).
         */
        post("/notification") {
            try {
                val payload = call.receive<P24NotificationPayload>()
                p24Service.handleNotification(payload)
                call.respondText("OK", status = HttpStatusCode.OK)
            } catch (e: P24Exception) {
                logger.warn("P24 notification rejected: {}", e.message)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadRequest,
                    ApiErrorResponse(e.message ?: "P24 notification error"),
                )
            } catch (e: Exception) {
                logger.error("P24 notification failed", e)
                call.respond(HttpStatusCode.InternalServerError, ApiErrorResponse("Notification processing failed"))
            }
        }
    }
}
