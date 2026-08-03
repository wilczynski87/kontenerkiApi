package com.kontenery.controller

import com.kontenery.p24.dto.P24CreateForClientRequest
import com.kontenery.p24.dto.P24CreateTransactionRequest
import com.kontenery.p24.dto.P24NotificationPayload
import com.kontenery.p24.exception.P24Exception
import com.kontenery.p24.service.P24Service
import com.kontenery.utils.ApiErrorResponse
import com.kontenery.utils.isValidInternalApiKey
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("P24Controller")

private const val DEFAULT_URL_RETURN = "https://kontenery-magazynowe.pl"

/**
 * JWT-protected routes for Magazynki (clientId from access token).
 */
fun Route.p24ClientRoutes(p24Service: P24Service) {
    route("/p24") {
        post("/transactions/forClient") {
            val userId = call.principal<JWTPrincipal>()
                ?.payload
                ?.getClaim("userId")
                ?.asString()
                ?.trim()
                .orEmpty()
            val clientId = userId.toLongOrNull()
            if (clientId == null || clientId <= 0L) {
                // 400 — nie 401: Magazynki traktuje 401 jako wygasłą sesję i wylogowuje
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiErrorResponse(
                        if (userId == "0" || clientId == 0L) {
                            "Konto administracyjne nie może tworzyć płatności klienta"
                        } else {
                            "Nieprawidłowy identyfikator klienta w tokenie"
                        },
                    ),
                )
                return@post
            }
            try {
                val body = call.receive<P24CreateForClientRequest>()
                val request = P24CreateTransactionRequest(
                    clientId = clientId,
                    amount = body.amount,
                    email = body.email,
                    description = body.description,
                    invoiceNumbers = body.invoiceNumbers,
                    urlReturn = body.urlReturn?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_URL_RETURN,
                    currency = body.currency,
                    language = body.language,
                    country = body.country,
                )
                val response = p24Service.createTransaction(request)
                call.respond(HttpStatusCode.Created, response)
            } catch (e: P24Exception) {
                logger.warn("P24 forClient create failed: {}", e.message)
                call.respond(
                    e.statusCode?.let { HttpStatusCode.fromValue(it) } ?: HttpStatusCode.BadGateway,
                    ApiErrorResponse(e.message ?: "P24 error"),
                )
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiErrorResponse(e.message ?: "Bad request"))
            }
        }
    }
}

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
