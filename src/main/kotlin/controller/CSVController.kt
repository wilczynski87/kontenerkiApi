package com.kontenery.controller

import com.kontenery.library.model.Payment
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.kontenery.library.utils.MessageRequest
import com.kontenery.service.CSVService
import com.kontenery.service.PaymentService
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory

fun Route.CSVController(csvService: CSVService, paymentService: PaymentService) {
    val logger = LoggerFactory.getLogger("CSVController")
    route("/csv") {

        post("/PeKaOSA") {
            try {
                println("POST CSV")
                val rawCSV: MessageRequest = call.receive<MessageRequest>()
//                println("rawCSV: ${rawCSV.message}")

                val payments: List<Payment> = csvService.readCSV(rawCSV.message)
//                payments.forEach { println(it) }

                coroutineScope {
                    payments.filterNot { it.fromClient == null }
                        .map { it.toDto() }
                        .forEach {
                            try {
                                paymentService.createPayment(it)
                            } catch (e: Exception) {
                                logger.error("createPayment error")
                            }
                        }
                }

                call.respond(HttpStatusCode.OK)
            } catch(e:Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
    }
}