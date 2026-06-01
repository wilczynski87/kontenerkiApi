package com.kontenery.service.impl

import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.InvoiceSend
import com.kontenery.data.utils.now
import com.kontenery.service.PrintService
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.rmi.ServerException

private val logger = LoggerFactory.getLogger("PrintServiceImpl")

class PrintServiceImpl(
    emailName: String,
    emailPort: String,
    private val client: HttpClient = HttpClient(),
) : PrintService {
    private val emailContainerAddress = "http://$emailName:$emailPort/sendMailWithAttachment/withVat"
    private val printInvoicesAddress = "http://$emailName:$emailPort/printInvoices"
    private val sendInvoiceAgain = "http://$emailName:$emailPort/sendMailWithAttachment/sendInvoiceAgain"
    private val rentIncrease = "http://$emailName:$emailPort/sendMailWithAttachment/rentIncrease"


    override suspend fun sendPeriodicInvoice(invoice: Invoice) {
        try {
            val json: String = Json.encodeToString(invoice)

            val response = client.post(emailContainerAddress) {
                contentType(ContentType.Application.Json)
                setBody(json)
            }
            if (!response.status.isSuccess()) {
                logger.error("sendPeriodicInvoice failed with status ${response.status}: ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            logger.error("sendPeriodicInvoice EXCEPTION, invoiceNumber: ${invoice.invoiceNumber}", e)
        }
    }

    override suspend fun printInvoices(invoices: List<Invoice>) {
        try {
            val jsons: String = Json.encodeToString(invoices)

            val response = client.post(printInvoicesAddress) {
                contentType(ContentType.Application.Json)
                setBody(jsons)
            }
            if (!response.status.isSuccess()) {
                logger.error("printInvoices failed with status ${response.status}: ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            logger.error("printInvoices EXCEPTION, invoiceNumbers: ${invoices.map { it.invoiceNumber }}", e)
        }
    }

    override suspend fun sendUtilitiesInvoice(invoice: Invoice) {
        TODO("Not yet implemented")
    }

    override suspend fun sendPeriodicBill(invoice: Invoice) {
        TODO("Not yet implemented")
    }

    override suspend fun sendInvoiceAgain(invoice: Invoice): InvoiceSend {
        val invoiceSend = InvoiceSend(
            invoice.invoiceNumber,
            invoice.customer?.name,
            invoice.invoiceSendToClient,
            LocalDate.now()
        )
        return try {
            val json: String = Json.encodeToString(invoice)

            val response = client.post(sendInvoiceAgain) {
                contentType(ContentType.Application.Json)
                setBody(json)
            }

            if (!response.status.isSuccess()) {
                throw IllegalArgumentException("Email service returned ${response.status}: ${response.bodyAsText()}")
            }
            
            invoiceSend.copy(sendLastTime = LocalDate.now())

        } catch (e: Exception) {
            logger.error("sendInvoiceAgain EXCEPTION, invoiceNumber: ${invoice.invoiceNumber}", e)
            throw ServerException("Could not send Invoice by email: ${invoice.invoiceNumber}")
        }
    }

}
