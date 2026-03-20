package com.kontenery.controller

import com.kontenery.library.model.Payment
import com.kontenery.library.model.PaymentDto
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.now
import com.kontenery.library.utils.startOfCurrentYear
import com.kontenery.service.PaymentService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import kotlinx.datetime.LocalDate

fun Route.paymentRoute(paymentService: PaymentService) {
    route("payment") {
        get("/{clientId}/byClient") {
            try {
                val clientId: Long = call.pathParameters["clientId"]?.toLong() ?: throw NullPointerException("There is no clientId")
                val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
                val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100
                val from: LocalDate = call.queryParameters["from"]?.let { LocalDate.parse(it) } ?: LocalDate.startOfCurrentYear()
                val to: LocalDate = call.queryParameters["to"]?.let { LocalDate.parse(it) } ?: LocalDate.now()

                val payments: List<Payment> = paymentService.getPaymentsByClient(
                    page = page,
                    size = size,
                    clientId = clientId,
                    from = from,
                    to = to
                )
                payments.forEach { println(it) }

                call.respond(payments)

            } catch (e:Exception) {
                println("payment/{clientId}/byClient")
                println(e)
                call.respond(HttpStatusCode.ExpectationFailed)
            }
        }

        get("/{clientId}/forClient") {
            try {
                val clientId: Long = call.pathParameters["clientId"]?.toLongOrNull()
                    ?: throw NullPointerException("Brak Id klienta")
                val from: LocalDate = call.queryParameters["from"]?.let { LocalDate.parse(it) } ?: LocalDate.startOfCurrentYear()
                val to: LocalDate = call.queryParameters["to"]?.let { LocalDate.parse(it) } ?: LocalDate.now()

                val payments: List<Payment> = paymentService.getPaymentsByClient(
                    clientId = clientId,
                    from = from,
                    to = to
                )

                call.respond(payments)

            } catch (e: Exception) {
                println(e)
                call.respond(e)
            }
        }

        post() {
            try {
                val paymentDtoReceived = call.receive<PaymentDto>()
                println("paymentDtoReceived: $paymentDtoReceived")
                val paymentSaved: Payment = paymentService.createPayment(paymentDtoReceived)

                call.respond(paymentSaved)

            } catch (e:Exception) {
                println("payment/post EXCEPTION")
                println(e)
                call.respond(HttpStatusCode.ExpectationFailed)
            }
        }

        put() {
            try {
                val paymentDtoReceived = call.receive<PaymentDto>()

                val paymentSaved: Payment = paymentService.updatePayment(paymentDtoReceived)

                call.respond(paymentSaved)

            } catch (e:Exception) {
                println("payment/put")
                println(e)
                call.respond(HttpStatusCode.ExpectationFailed)
            }
        }

        delete("/{paymentId}/delete") {
            try {
                val paymentId: Long = call.pathParameters["paymentId"]?.toLong() ?: throw NullPointerException("There is no paymentId")

                val paymentSaved: Boolean = paymentService.deletePayment(paymentId)

                call.respond(paymentSaved)
            } catch (e:Exception) {
                println("payment/{paymentId}/delete")
                println(e)
                call.respond(HttpStatusCode.ExpectationFailed)
            }
        }
    }
}