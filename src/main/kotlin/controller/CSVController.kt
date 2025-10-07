package com.kontenery.controller

import com.kontenery.library.model.Payment
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.kontenery.library.utils.MessageRequest
import com.kontenery.library.utils.errors.PaymentError
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

                coroutineScope {
                    val newPayments: List<Payment> = csvService.readCSV(rawCSV.message)
//                payments.forEach { println("payment: $it") }

                    val errors: MutableList<PaymentError> = mutableListOf()
                    newPayments.filterNot { it.fromClient == null }
                        .filter { paymentService.validatePayment(it, errors) }
                        .map { it.toDto() }
                        .forEach {
                            try {
//                                println("it: ${it.referenceNumber}")
                                paymentService.createPayment(it)
                            } catch (e: Exception) {
                                logger.error("createPayment error")
                            }
                        }
                }

                call.respond(MessageRequest("OK"))
            } catch(e:Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        post("/Alior") {
            try {
                val rawCSV: MessageRequest = call.receive<MessageRequest>()
//                println("rawCSV: $rawCSV")
                coroutineScope {
                    val newPayments: List<Payment> = csvService.readCSVAlior(rawCSV.message)
                    newPayments.forEach { println("payment: $it") }
                }
                call.respond(MessageRequest("OK"))
            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
    }
}