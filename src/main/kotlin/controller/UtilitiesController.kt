package com.kontenery.controller

import com.kontenery.library.model.ContractDto
import com.kontenery.library.model.Reading
import com.kontenery.library.model.Submeter
import com.kontenery.service.UtilitiesService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import io.ktor.server.response.respond

fun Route.utilitiesController(utilitiesService: UtilitiesService) {
    val logger:Logger = LoggerFactory.getLogger("utilitiesController")
    route("utilities") {
        get("/submeters") {
            try {
                call.respond(utilitiesService.getSubmeters())
            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(e.message.toString())
            }
        }
        get("/submeter") {
            try {
                val submeterId:Long = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                val result = utilitiesService.getSubmeter(submeterId) ?: throw NullPointerException("nie znaleziono podlicznika")

                call.respond(result)

            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(e.message.toString())
            }
        }
        post("/submeter") {
            try {
                val submeter: Submeter = call.receive<Submeter>()
                val result = utilitiesService.postSubmeter(submeter) ?: throw NullPointerException("nie można zapisać podlicznika")

                call.respond(result)

            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(e.message.toString())
            }
        }
        put("{id}/submeter") {
            try {
                val submeter: Submeter = call.receive<Submeter>()
                val submeterId: Long = call.parameters["id"]?.toLongOrNull() ?: throw NullPointerException("nie można odczytać ID podlicznika")
                val result = utilitiesService.updateSubmeter(submeterId, submeter) ?: throw NullPointerException("nie można zaktualizować")

                call.respond(result)

            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(e.message.toString())
            }
        }
        delete("/submeter") {
            try {
                val submeterId: Long = call.parameters["id"]?.toLongOrNull() ?: throw NullPointerException("nie można odczytać ID podlicznika")
                val result = utilitiesService.deleteSubmeter(submeterId)

                call.respond(result)

            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(e.message.toString())
            }
        }


        get("/reading") {
            try {
                val readingId:Long = call.parameters["id"]?.toLongOrNull() ?: throw NullPointerException("nie można odczytać ID odczytu")

                val result = utilitiesService.getReading(readingId) ?: throw NullPointerException("nie można znaleźć odczytu")
                call.respond(result)
            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(e.message.toString())
            }
        }
        get("/readings") {
            try {
                call.respond(utilitiesService.getReadings())
            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(e.message.toString())
            }
        }
        post("/reading") {
            try {
                val reading: Reading = call.receive<Reading>()
                val result = utilitiesService.postReading(reading) ?: throw NullPointerException("nie można zapisać odczytu")

                call.respond(result)

            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(e.message.toString())
            }
        }
        put("/reading") {
            try {
                val readingId:Long = call.parameters["id"]?.toLongOrNull() ?: throw NullPointerException("nie można odczytać ID odczytu")
                val reading: Reading = call.receive<Reading>()
                val result = utilitiesService.updateReading(readingId, reading) ?: throw NullPointerException("nie można zapisać odczytu")

                call.respond(result)

            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(e.message.toString())
            }
        }
        delete("/reading") {
            try {
                val readingId:Long = call.parameters["id"]?.toLongOrNull() ?: throw NullPointerException("nie można odczytać ID odczytu")

                call.respond(utilitiesService.deleteSubmeter(readingId))
            } catch (e: Exception) {
                logger.error(e.message)
                call.respond(e.message.toString())
            }
        }

    }

}