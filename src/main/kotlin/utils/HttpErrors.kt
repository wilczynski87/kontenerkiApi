package com.kontenery.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

@Serializable
data class ApiErrorResponse(val error: String)

private val log = LoggerFactory.getLogger("HttpErrors")

suspend fun ApplicationCall.respondInternalError(
    cause: Throwable,
    userMessage: String = "Internal server error",
) {
    log.error(userMessage, cause)
    respond(HttpStatusCode.InternalServerError, ApiErrorResponse(userMessage))
}

suspend fun ApplicationCall.respondBadRequest(
    userMessage: String = "Bad request",
    cause: Throwable? = null,
) {
    cause?.let { log.warn(userMessage, it) }
    respond(HttpStatusCode.BadRequest, ApiErrorResponse(userMessage))
}

suspend fun ApplicationCall.respondUnauthorized(
    userMessage: String = "Unauthorized",
    cause: Throwable? = null,
) {
    cause?.let { log.warn(userMessage, it) }
    respond(HttpStatusCode.Unauthorized, ApiErrorResponse(userMessage))
}
