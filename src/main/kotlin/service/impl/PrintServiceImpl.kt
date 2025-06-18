package com.kontenery.service.impl

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.service.PrintService
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

val emailName:String = System.getenv("EMAIL_NAME") ?: throw NullPointerException("There is no email address")
val emailPort:String = System.getenv("EMAIL_PORT") ?: throw NullPointerException("There is no email port")

class PrintServiceImpl: PrintService {
    val client = HttpClient()
    private val emailContainerAddress = "http://$emailName:$emailPort/sendMailWithAttachment/withVat"

    override suspend fun sendPeriodicInvoice(invoice: Invoice) {
        try {

//            println("email container address: ")
//            println(emailContainerAddress)
//            println("email container address: ")
//            println(emailContainerAddress)

            val json:String = Json.encodeToString(invoice)
            client.post(emailContainerAddress) {
                contentType(ContentType.Application.Json)
                setBody(json)
            }
        } catch (e:Exception) {
            println("sendPeriodicInvoice EXCEPTION, invoiceNumber: ${invoice.invoiceNumber}")
            println(e)
        }
    }

    override suspend fun printInvoices(from: LocalDate, to: LocalDate) {
        TODO("Not yet implemented")
    }

    override suspend fun sendUtilitiesInvoice(invoice: Invoice) {
        TODO("Not yet implemented")
    }

    override suspend fun sendPeriodicBill(invoice: Invoice) {
        TODO("Not yet implemented")
    }

}