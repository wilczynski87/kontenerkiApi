package com.kontenery.controller

import com.kontenery.service.InvoiceService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import model.MailSendParam

fun Route.mailSendConfirmation(invoiceService: InvoiceService) {
    route("/mailSend") {
        get("/invoice") {
            println("mailSend/invoice")
            try {
                val key = call.request.headers["X-Internal-Key"]
                val expected = System.getenv("INTERNAL_API_KEY")
                if (key != expected) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }
                val invoiceNumber: String = call.queryParameters[MailSendParam.INVOICE_NUMBER.param].toString()
                val invoiceSendDate: LocalDate = LocalDate.parse(call.queryParameters[MailSendParam.SEND_DATE.param].toString())
                println("mailSend/invoice: invoice send: $invoiceNumber, on date: $invoiceSendDate")

                invoiceService.confirmInvoiceSendDate(invoiceNumber, invoiceSendDate)

                call.respond(HttpStatusCode.OK)
            } catch (e:Exception) {
                println("mailSendConfirmation/invoice error: $e")
                call.respondNullable(HttpStatusCode.ExpectationFailed, e.message)
            }
        }
    }
}