package com.kontenery.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.bramaController() {
    route("brama") {
        get("access") {
            try {
                val result:String = call.receive()
                println("brama/access Success: $result")
                call.respond("success $result")
            } catch (e: Exception) {
                println("brama/access EXCEPTION: $e")

                call.respond(HttpStatusCode.ExpectationFailed, "EXCEPTION $e", )
            }
        }
    }
}