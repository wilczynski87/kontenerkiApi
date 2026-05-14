package com.kontenery.controller

import com.kontenery.data.Payment
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.kontenery.data.utils.MessageRequest
import com.kontenery.data.utils.errors.PaymentError
import com.kontenery.service.CSVService
import com.kontenery.service.PaymentService
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory

fun Route.CSVController(csvService: CSVService, paymentService: PaymentService) {
    val logger = LoggerFactory.getLogger("CSVController")
    route("/csv") {

        post("/PeKaOSA") {
            try {
                println("POST CSV PeKaOSA")
                val rawCSV: MessageRequest = call.receive<MessageRequest>()
//                println("rawCSV: ${rawCSV.message}")

                coroutineScope {
                    val newPayments: List<Payment> = csvService.readCSV(rawCSV.message)
//                    newPayments.forEach { println("payment: $it") }

                    val errors: MutableList<PaymentError> = mutableListOf()
                    newPayments.filterNot { it.fromClient == null }
                        .filter { paymentService.validatePayment(it, errors) }
                        .map { it.toDto() }
                        .forEach {
                            try {
//                                println("it: ${it.referenceNumber}")
                                paymentService.createPayment(it)
                                logger.info("payment created: ${it.paymentId}")
                            } catch (e: Exception) {
//                                logger.error("createPayment error: id:${it.paymentId} \n ${e.message}")
                            }
                        }
                    errors.forEach { error ->
                        logger.info("payment error: ${error.title} - ${error.message}, client: ${error.payment?.fromClient?.getName()} ${error.payment?.date} ${error.payment?.amount}")
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
                println("POST CSV Alior")
                val rawCSV: MessageRequest = call.receive<MessageRequest>()
//                println("rawCSV: $rawCSV")
                val errors: MutableList<PaymentError> = mutableListOf()
                coroutineScope {
                    val newPayments: List<Payment> = csvService.readCSVAlior(rawCSV.message)
                        .filter { paymentService.validatePaymentByParams(it, errors) }
                    newPayments.forEach { println("payment: $it") }
                }
                call.respond(MessageRequest("OK"))
            } catch (e: Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }

        post("/Nest") {
            try {
                println("POST CSV Nest")
                val rawCSV: MessageRequest = call.receive<MessageRequest>()
//                println("rawCSV: ${rawCSV.message}")

                coroutineScope {
                    val newPayments: List<Payment> = csvService.readCSVNest(rawCSV.message)
//                    newPayments.forEach { println("payment: $it") }

                    val errors: MutableList<PaymentError> = mutableListOf()
                    newPayments.filterNot { it.fromClient == null }
                        .filter { paymentService.validatePayment(it, errors) }
                        .map { it.toDto() }
                        .forEach {
                            try {
//                                println("it: ${it.referenceNumber}")
                                paymentService.createPayment(it)
                                logger.info("payment created: $it " + ""
//                                        "${it.paymentId}, ${it.title}, ${it.fromClientId}"
                                )
                            } catch (e: Exception) {
//                                logger.error("createPayment error: id:${it.paymentId} \n ${e.message}")
                            }
                        }
                    errors.forEach { error ->
                        logger.info("payment error: ${error.title} - ${error.message}, client: ${error.payment?.fromClient?.getName()} ${error.payment?.date} ${error.payment?.amount}")
                    }
                }

                call.respond(MessageRequest("OK"))
            } catch(e:Exception) {
                println(e)
                call.respond(HttpStatusCode.BadRequest, "Invalid request: ${e.message}")
            }
        }
    }
}