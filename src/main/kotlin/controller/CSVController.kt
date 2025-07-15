package com.kontenery.controller

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.CSVController() {


    route("/csv") {

        post("/PeKaOSA") {
            try {
                println("POST CONTRACT")
                val rawCSV: String = call.receive<String>()

                println("rawCSV: $rawCSV")
                call.respond(HttpStatusCode.OK)
            } catch(e:Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
    }
}