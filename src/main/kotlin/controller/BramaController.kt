package com.kontenery.controller

import com.kontenery.utils.ApiErrorResponse
import com.kontenery.utils.isValidInternalApiKey
import com.kontenery.utils.respondInternalError
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.slf4j.LoggerFactory

private val bramaLog = LoggerFactory.getLogger("BramaController")

fun Route.bramaController() {
    route("brama") {
        get("access") {
            if (!call.isValidInternalApiKey()) {
                call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("Unauthorized"))
                return@get
            }
            try {
                call.receive<String>()
                call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
            } catch (e: Exception) {
                call.respondInternalError(e, "Gate access failed")
            }
        }
    }
}
