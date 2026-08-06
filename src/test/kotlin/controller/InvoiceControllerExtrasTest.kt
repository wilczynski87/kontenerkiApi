package com.kontenery.controller

import com.kontenery.data.Client
import com.kontenery.data.ClientCompanyData
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.InvoiceSend
import com.kontenery.data.utils.InvoiceType
import com.kontenery.ksef.dto.KsefInvoiceRegisteredResponse
import com.kontenery.ksef.service.KsefService
import com.kontenery.service.ClientService
import com.kontenery.service.InvoiceService
import com.kontenery.service.PrintService
import com.kontenery.testfixtures.sampleNonVatInvoice
import com.kontenery.testfixtures.sampleVatInvoice
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InvoiceControllerExtrasTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val vatClient = Client(
        id = 21L,
        isActive = true,
        clientCompany = ClientCompanyData(name = "Aurora sp. z o.o.", needInvoice = true),
    )

    @Test
    fun `POST invoice custom returns saved invoice`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val printService = mockk<PrintService>(relaxUnitFun = true)
        val clientService = mockk<ClientService>()
        val ksefService = mockk<KsefService>()

        val requestInvoice = sampleNonVatInvoice().copy(
            invoiceNumber = null,
            type = InvoiceType.OTHER.name,
            customer = sampleVatInvoice().customer,
        )
        val createdInvoice = requestInvoice.copy(
            invoiceNumber = "R/8/2026",
            invoiceDate = LocalDate(2026, 8, 1),
            customer = requestInvoice.customer?.copy(client = vatClient),
        )
        val savedInvoice = createdInvoice.copy(invoiceSendToClient = LocalDate(2026, 8, 2))

        coEvery { clientService.findClientById(21L) } returns vatClient
        coEvery { invoiceService.createCustomInvoice(any()) } returns createdInvoice
        coEvery { invoiceService.getInvoiceByNumber("R/8/2026") } returns null
        coEvery { invoiceService.saveInvoice(createdInvoice) } returns savedInvoice
        coEvery { ksefService.isInvoiceRegisteredInKsef("R/8/2026") } returns KsefInvoiceRegisteredResponse(
            invoiceNumber = "R/8/2026",
            registered = false,
        )

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    invoiceRoutes(invoiceService, printService, clientService, ksefService)
                }
            }
            val response = client.post("/invoice/21/custom") {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(json.encodeToString(requestInvoice))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("R/8/2026"))
            assertTrue(body.contains("Aurora sp. z o.o."))
        }

        coVerify(exactly = 1) { invoiceService.createCustomInvoice(any()) }
        coVerify(exactly = 1) { invoiceService.saveInvoice(createdInvoice) }
        coVerify(exactly = 1) { printService.sendPeriodicInvoice(savedInvoice) }
    }

    @Test
    fun `POST sendAgain returns invoice send payload`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val printService = mockk<PrintService>()
        val clientService = mockk<ClientService>()
        val ksefService = mockk<KsefService>()

        val invoice = sampleVatInvoice(invoiceNumber = "12/8/2026").copy(
            type = InvoiceType.PERIODIC.name,
            invoiceDate = LocalDate(2026, 8, 1),
        )
        val sendResult = InvoiceSend(
            invoiceNumber = "12/8/2026",
            forClient = "Aurora sp. z o.o.",
            sendFirstTime = LocalDate(2026, 8, 1),
            sendLastTime = LocalDate(2026, 8, 6),
        )

        coEvery { invoiceService.getInvoiceByNumber("12/8/2026") } returns invoice
        coEvery { ksefService.isInvoiceRegisteredInKsef("12/8/2026") } returns KsefInvoiceRegisteredResponse(
            invoiceNumber = "12/8/2026",
            registered = true,
            ksefNumber = "KSEF-1",
        )
        coEvery { printService.sendInvoiceAgain(invoice) } returns sendResult

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    invoiceRoutes(invoiceService, printService, clientService, ksefService)
                }
            }
            val response = client.post("/invoice/sendAgain") {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("12/8/2026")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("Aurora sp. z o.o."))
            assertTrue(body.contains("12/8/2026"))
        }

        coVerify(exactly = 1) { invoiceService.getInvoiceByNumber("12/8/2026") }
        coVerify(exactly = 1) { printService.sendInvoiceAgain(invoice) }
    }
}
