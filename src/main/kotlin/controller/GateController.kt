package com.kontenery.controller

import com.kontenery.data.gate.OpenGateRequest
import com.kontenery.service.GateAccessDeniedException
import com.kontenery.service.GateService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.gate(gateService: GateService) {
    route("/gate") {
        post("/open") {
            val jwtUserId = call.principal<JWTPrincipal>()
                ?.payload
                ?.getClaim("userId")
                ?.asString()

            println("jwtUserId: $jwtUserId")

//            val request = runCatching { call.receive<OpenGateRequest>() }
//                .getOrElse { OpenGateRequest() }

            try {
                val clientId = gateService.checkUserAuthenticated(jwtUserId)
//                gateService.ensureActiveContract(clientId)
                gateService.ensureNoOverdue(clientId)
                gateService.ensureCooldown(clientId)

                val response = gateService.openGate()
//                gateService.logOpenEvent(clientId)

                call.respond(response)
            } catch (e: GateAccessDeniedException) {
                val status = if (e.message?.contains("zalogowany", ignoreCase = true) == true) {
                    HttpStatusCode.Unauthorized
                } else {
                    HttpStatusCode.Forbidden
                }
                call.respond(status, mapOf("error" to (e.message ?: "Brak dostępu")))
            }
        }
        post("/openTest") {
            val jwtUserId = call.principal<JWTPrincipal>()
                ?.payload
                ?.getClaim("userId")
                ?.asString()
            println("jwtUserId: $jwtUserId")
            call.respond(jwtUserId!!)
        }
    }
}
