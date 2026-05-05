package com.kontenery.service.impl

import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.InvoiceSend
import com.kontenery.data.utils.now
import com.kontenery.service.PrintService
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.rmi.ServerException

private val logger = LoggerFactory.getLogger("PrintServiceImpl")

class PrintServiceImpl(emailName: String, emailPort: String): PrintService {
//    val emailName:String = env["EMAIL_HOST"] ?: throw NullPointerException("There is no email address")
//    val emailPort:String = env["EMAIL_PORT"] ?: throw NullPointerException("There is no email port")

    val client = HttpClient()
    private val emailContainerAddress = "http://$emailName:$emailPort/sendMailWithAttachment/withVat"
    private val printInvoicesAddress = "http://$emailName:$emailPort/printInvoices"
    private val sendInvoiceAgain = "http://$emailName:$emailPort/sendMailWithAttachment/sendInvoiceAgain"
    private val rentIncrease = "http://$emailName:$emailPort/sendMailWithAttachment/rentIncrease"


    override suspend fun sendPeriodicInvoice(invoice: Invoice) {
        try {
            val json:String = Json.encodeToString(invoice)
//            println("invoice: $json")

            client.post(emailContainerAddress) {
                contentType(ContentType.Application.Json)
                setBody(json)
            }
        } catch (e:Exception) {
            logger.error("sendPeriodicInvoice EXCEPTION, invoiceNumber: ${invoice.invoiceNumber}\n $e")
        }
    }

    override suspend fun printInvoices(invoices: List<Invoice>) {
        try {
            val jsons: String = Json.encodeToString(invoices)

            client.post(printInvoicesAddress) {
                contentType(ContentType.Application.Json)
                setBody(jsons)
            }
        } catch (e:Exception) {
            println("printInvoices EXCEPTION, invoiceNumber: ${invoices.map { it.invoiceNumber }}")
            println(e)
        }
    }

    override suspend fun sendUtilitiesInvoice(invoice: Invoice) {
        TODO("Not yet implemented")
    }

    override suspend fun sendPeriodicBill(invoice: Invoice) {
        TODO("Not yet implemented")
    }

    override suspend fun sendInvoiceAgain(invoice: Invoice): InvoiceSend {
        val invoiceSend = InvoiceSend(invoice.invoiceNumber, invoice.customer?.name, invoice.invoiceSendToClient,
            LocalDate.now())
        return try {
            val json:String = Json.encodeToString(invoice)

            val statusSend = client.post(sendInvoiceAgain) {
                contentType(ContentType.Application.Json)
                setBody(json)
            }.status

            if(statusSend.isSuccess().not()) throw IllegalArgumentException(statusSend.description)
            else invoiceSend.copy(sendLastTime = LocalDate.now())

        } catch (e:Exception) {
            println("\nsendInvoiceAgain EXCEPTION, invoiceNumber: ${invoice.invoiceNumber}")
            println(e)
            throw ServerException("Could not send Invoice by email: ${invoiceSend}, for Cl")
        }
    }

}