package com.kontenery.service.impl

import com.kontenery.testfixtures.sampleVatInvoice
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.rmi.ServerException

class PrintServiceImplTest {

    @Test
    fun `sendPeriodicInvoice does not throw when email service fails`() = runBlocking {
        val client = HttpClient(
            MockEngine {
                respond(
                    content = "error",
                    status = HttpStatusCode.InternalServerError,
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                )
            },
        )
        val service = PrintServiceImpl("email", "9000", client)

        service.sendPeriodicInvoice(sampleVatInvoice())
    }

    @Test
    fun `sendInvoiceAgain returns send time on success`() = runBlocking {
        val client = HttpClient(
            MockEngine { request ->
                assertEquals("/sendMailWithAttachment/sendInvoiceAgain", request.url.encodedPath)
                respond(
                    content = "OK",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                )
            },
        )
        val service = PrintServiceImpl("email", "9000", client)
        val invoice = sampleVatInvoice()

        val result = service.sendInvoiceAgain(invoice)

        assertEquals(invoice.invoiceNumber, result.invoiceNumber)
        assertEquals(invoice.customer?.name, result.forClient)
        assertNotNull(result.sendLastTime)
    }

    @Test
    fun `sendInvoiceAgain throws ServerException when email service returns error`() {
        val client = HttpClient(
            MockEngine {
                respond(
                    content = "mail down",
                    status = HttpStatusCode.BadGateway,
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                )
            },
        )
        val service = PrintServiceImpl("email", "9000", client)

        assertThrows(ServerException::class.java) {
            runBlocking { service.sendInvoiceAgain(sampleVatInvoice()) }
        }
    }
}
