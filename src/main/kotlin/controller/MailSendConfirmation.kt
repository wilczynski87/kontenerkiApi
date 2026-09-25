package com.kontenery.controller

import com.kontenery.service.InvoiceService
import com.kontenery.utils.ApiErrorResponse
import com.kontenery.utils.isValidInternalApiKey
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDate
import model.MailSendParam
import org.slf4j.LoggerFactory

private val mailSendLog = LoggerFactory.getLogger("MailSendConfirmation")

fun Route.mailSendConfirmation(invoiceService: InvoiceService) {
    route("/mailSend") {
        // Error callback from email service (GET /mailSend?invoiceNumber=...&error=ERROR&message=...)
        get {
            if (!call.isValidInternalApiKey()) {
                call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("Unauthorized"))
                return@get
            }

            val invoiceNumber = call.queryParameters[MailSendParam.INVOICE_NUMBER.param]
            val sendDate = call.queryParameters[MailSendParam.SEND_DATE.param]
            val error = call.queryParameters[MailSendParam.ERROR.param]
            val message = call.queryParameters[MailSendParam.MESSAGE.param]

            mailSendLog.error(
                "Mail send failed: invoice={}, date={}, error={}, message={}",
                invoiceNumber,
                sendDate,
                error,
                message,
            )

            call.respond(HttpStatusCode.OK)
        }

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
