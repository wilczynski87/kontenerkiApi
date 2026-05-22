package com.kontenery

import com.kontenery.utils.ApiErrorResponse
import com.kontenery.utils.respondInternalError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory

@Serializable
data class ErrorResponse(
    val status: Int,
    val errors: List<String>
)

private val statusPagesLog = LoggerFactory.getLogger("StatusPages")

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    errors = cause.reasons
                )
            )
        }
        exception<BadRequestException> { call, cause ->
            statusPagesLog.warn("Bad request", cause)
            call.respond(HttpStatusCode.BadRequest, ApiErrorResponse("Bad request"))
        }
        exception<NotFoundException> { call, cause ->
            statusPagesLog.warn("Not found", cause)
            call.respond(HttpStatusCode.NotFound, ApiErrorResponse("Not found"))
        }
        exception<Throwable> { call, cause ->
            call.respondInternalError(cause)
        }
    }
}
