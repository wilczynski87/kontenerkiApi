package com.kontenery.controller

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.kontenery.library.utils.MessageRequest
import com.kontenery.service.CSVService

fun Route.CSVController(csvService: CSVService) {

    route("/csv") {

        post("/PeKaOSA") {
            try {
                println("POST CONTRACT")
                val rawCSV: MessageRequest = call.receive<MessageRequest>()

//                println("rawCSV: ${rawCSV.message}")

                csvService.readLinesFromSCV(rawCSV.message)

                call.respond(HttpStatusCode.OK)
            } catch(e:Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
    }
}