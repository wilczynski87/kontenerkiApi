package com.kontenery.controller

import com.kontenery.service.InvoiceService
import com.kontenery.utils.ApiErrorResponse
import com.kontenery.utils.isValidInternalApiKey
import com.kontenery.utils.respondInternalError
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import model.MailSendParam
import org.slf4j.LoggerFactory

private val mailSendLog = LoggerFactory.getLogger("MailSendConfirmation")

fun Route.mailSendConfirmation(invoiceService: InvoiceService) {
    route("/mailSend") {
        get("/invoice") {
            if (!call.isValidInternalApiKey()) {
                call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("Unauthorized"))
                return@get
            }
            try {
                val invoiceNumber: String = call.queryParameters[MailSendParam.INVOICE_NUMBER.param].toString()
                val invoiceSendDate: LocalDate = LocalDate.parse(call.queryParameters[MailSendParam.SEND_DATE.param].toString())

                invoiceService.confirmInvoiceSendDate(invoiceNumber, invoiceSendDate)

                call.respond(HttpStatusCode.OK)
            } catch (e: Exception) {
                mailSendLog.error("mailSend/invoice failed for invoice confirmation", e)
                call.respond(
                    HttpStatusCode.ExpectationFailed,
                    ApiErrorResponse("Invoice send confirmation failed"),
                )
            }
        }
    }
}
