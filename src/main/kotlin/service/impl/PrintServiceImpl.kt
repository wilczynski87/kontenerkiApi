package com.kontenery.service.impl

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.service.PrintService
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

val emailName:String = System.getenv("EMAIL_HOST") ?: throw NullPointerException("There is no email address")
val emailPort:String = System.getenv("EMAIL_PORT") ?: throw NullPointerException("There is no email port")

private val logger = LoggerFactory.getLogger("PrintServiceImpl")

class PrintServiceImpl: PrintService {
    val client = HttpClient()
    private val emailContainerAddress = "http://$emailName:$emailPort/sendMailWithAttachment/withVat"
    private val printInvoicesAddress = "http://$emailName:$emailPort/printInvoices"
    private val sendInvoiceAgain = "http://$emailName:$emailPort/sendMailWithAttachment/sendInvoiceAgain"

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

    override suspend fun sendInvoiceAgain(invoice: Invoice) {
        try {
            val json:String = Json.encodeToString(invoice)

            client.post(sendInvoiceAgain) {
                contentType(ContentType.Application.Json)
                setBody(json)
            }
        } catch (e:Exception) {
            println("sendInvoiceAgain EXCEPTION, invoiceNumber: ${invoice.invoiceNumber}")
            println(e)
        }
    }

}