package com.kontenery.controller

import com.kontenery.data.Reading
import com.kontenery.data.ReadingDto
import com.kontenery.data.Submeter
import com.kontenery.data.SubmeterDto
import com.kontenery.data.invoice.Position
import com.kontenery.service.ClientService
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

fun Route.utilitiesController(
    utilitiesService: UtilitiesService,
    clientService: ClientService,
) {
    val logger: Logger = LoggerFactory.getLogger("utilitiesController")

    route("utilities") {

        // 🔹 SUBMETERS
        route("submeter") {
            get {
                try {
                    call.respond(utilitiesService.getSubmeters())
                } catch (e: Exception) {
                    logger.error("Błąd przy pobieraniu podliczników", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
            get("/{id}") {
                try {
                    val submeterId = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Brak lub błędne ID")
                    val result = utilitiesService.getSubmeter(submeterId)
                        ?: return@get call.respond(HttpStatusCode.ExpectationFailed, "Nie znaleziono podlicznika")

                    call.respond(result)
                } catch (e: Exception) {
                    logger.error("Błąd przy pobieraniu podlicznika", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
            post {
                try {
                    val submeter: Submeter = call.receive()
                    val result = utilitiesService.postSubmeter(submeter)
                        ?: return@post call.respond(HttpStatusCode.InternalServerError, "Nie można zapisać podlicznika")

                    call.respond(HttpStatusCode.Created, result)
                } catch (e: Exception) {
                    logger.error("Błąd przy tworzeniu podlicznika", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
            put("/{id}") {
                try {
                    val submeterId = call.parameters["id"]?.toLongOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "Brak lub błędne ID")
                    val submeter: SubmeterDto = call.receive()
                    val result = utilitiesService.updateSubmeter(submeterId, submeter.toSubmeter())
                        ?: return@put call.respond(HttpStatusCode.NotFound, "Nie można zaktualizować podlicznika")

                    call.respond(result)
                } catch (e: Exception) {
                    logger.error("Błąd przy aktualizacji podlicznika", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
            delete("/{id}") {
                try {
                    val submeterId = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, "Brak lub błędne ID")
                    val result = utilitiesService.deleteSubmeter(submeterId)

                    call.respond(result)
                } catch (e: Exception) {
                    logger.error("Błąd przy usuwaniu podlicznika", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
        }

        // 🔹 READINGS
        route("readings") {
            get("{id}/all") {
                try {
                    val submeterId = call.parameters["id"]?.toLongOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest, "Brak lub błędne ID")
                    call.respond(utilitiesService.getReadings(submeterId))
                } catch (e: Exception) {
                    logger.error("Błąd przy pobieraniu odczytów", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
            get("/{id}") {
                try {
                    val readingId = call.parameters["id"]?.toLongOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Brak lub błędne ID")
                    val result = utilitiesService.getReading(readingId)
                        ?: return@get call.respond(HttpStatusCode.NotFound, "Nie znaleziono odczytu")

                    call.respond(result)
                } catch (e: Exception) {
                    logger.error("Błąd przy pobieraniu odczytu", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
            post {
                try {
                    val reading: ReadingDto = call.receive()
                    // Validation
                    val submeterId: Long = reading.submeterId ?: throw NullPointerException("No submeter Id")
                    utilitiesService.getSubmeter(submeterId) ?: throw NullPointerException("No submeter found in database: $submeterId")

                    val result: Submeter = utilitiesService.addReading(reading.toReading())
                        ?: return@post call.respond(HttpStatusCode.InternalServerError, "Nie można zapisać odczytu")

                    call.respond(HttpStatusCode.Created, result)
                } catch (e: Exception) {
                    logger.error("Błąd przy tworzeniu odczytu", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }

            post("check/{clientId}") {
                try {
                    val reading: Reading = call.receive()
                    // Validation
                    val clientId: Long = call.parameters["clientId"]?.toLongOrNull() ?: throw NullPointerException("No client Id")
                    clientService.findClientById(clientId) ?: throw NullPointerException("No client found in database: $clientId")
                    val submeterId: Long = reading.submeterId ?: throw NullPointerException("No submeter Id")
                    utilitiesService.getSubmeter(submeterId) ?: throw NullPointerException("No submeter found in database: $submeterId")

                    // TODO
                    //  - czy kolejny odczyt nie będzie błędny
                    //      - np mniejsza wartość niż poprzedni odczyt
                    // - czy rodzaj mediów w odczycie i liczniku sie zgadzają
                    // - czy data się zgadza

//                    val result = utilitiesService.postReading(reading)
//                        ?: return@post call.respond(HttpStatusCode.InternalServerError, "Nie można zapisać odczytu")

                    call.respond(HttpStatusCode.Created, reading)
                } catch (e: Exception) {
                    logger.error("Błąd przy tworzeniu odczytu", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }

            post("createPosition/{clientId}") {
                try {
                    val readingDto: ReadingDto = call.receive()
                    val reading = readingDto.toReading()
                    // Validation
                    val clientId: Long = call.parameters["clientId"]?.toLongOrNull() ?: throw NullPointerException("No client Id")
                    clientService.findClientById(clientId) ?: throw NullPointerException("No client found in database: $clientId")
                    val submeterId: Long = reading.submeterId ?: throw NullPointerException("No submeter Id")
                    utilitiesService.getSubmeter(submeterId) ?: throw NullPointerException("No submeter found in database: $submeterId")

                    // TODO
                    //  - czy kolejny odczyt nie będzie błędny
                    //      - np mniejsza wartość niż poprzedni odczyt
                    // - czy rodzaj mediów w odczycie i liczniku sie zgadzają
                    // - czy data się zgadza

                    val result: Position = utilitiesService.createPosition(reading)
                        ?: return@post call.respond(HttpStatusCode.InternalServerError, "Nie można zapisać odczytu")

                    call.respond(HttpStatusCode.Created, result)
                } catch (e: Exception) {
                    logger.error("Błąd przy tworzeniu odczytu", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }

            put("/{id}") {
                try {
                    val readingId = call.parameters["id"]?.toLongOrNull()
                        ?: return@put call.respond(HttpStatusCode.BadRequest, "Brak lub błędne ID")
                    val reading: Reading = call.receive()
                    val result = utilitiesService.updateReading(readingId, reading)
                        ?: return@put call.respond(HttpStatusCode.NotFound, "Nie można zaktualizować odczytu")

                    call.respond(result)
                } catch (e: Exception) {
                    logger.error("Błąd przy aktualizacji odczytu", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
            delete("/{id}") {
                try {
                    val readingId = call.parameters["id"]?.toLongOrNull()
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, "Brak lub błędne ID")
                    val result = utilitiesService.deleteReading(readingId)

                    call.respond(result)
                } catch (e: Exception) {
                    logger.error("Błąd przy usuwaniu odczytu", e)
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Unknown error")))
                }
            }
        }
    }
}
