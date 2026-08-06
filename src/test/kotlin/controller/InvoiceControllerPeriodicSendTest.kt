package com.kontenery.controller

import com.kontenery.data.Client
import com.kontenery.data.ClientCompanyData
import com.kontenery.data.invoice.InvoiceSend
import com.kontenery.data.utils.InvoiceType
import com.kontenery.data.utils.errors.InvoiceErrorMessage
import com.kontenery.ksef.service.KsefService
import com.kontenery.service.ClientService
import com.kontenery.service.InvoiceService
import com.kontenery.service.PrintService
import com.kontenery.testfixtures.sampleVatInvoice
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
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
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.rmi.ServerException

class InvoiceControllerPeriodicSendTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val vatClient = Client(
        id = 21L,
        isActive = true,
        clientCompany = ClientCompanyData(name = "Aurora sp. z o.o.", needInvoice = true),
    )

    private val existingInvoice = sampleVatInvoice(invoiceNumber = "12/8/2026").copy(
        type = InvoiceType.PERIODIC.name,
        invoiceDate = LocalDate(2026, 8, 1),
        vatApply = true,
    )

    @Test
    fun `POST invoice clientId resends email when PERIODIC already exists`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val printService = mockk<PrintService>()
        val clientService = mockk<ClientService>()
        val ksefService = mockk<KsefService>()

        coEvery { clientService.findClientById(21L) } returns vatClient
        coEvery {
            invoiceService.findPeriodicDocumentForClient(21L, any(), true)
        } returns existingInvoice
        coEvery { printService.sendInvoiceAgain(existingInvoice) } returns InvoiceSend(
            invoiceNumber = existingInvoice.invoiceNumber,
            forClient = "Aurora",
            sendFirstTime = LocalDate(2026, 8, 1),
            sendLastTime = LocalDate(2026, 8, 3),
        )

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    invoiceRoutes(invoiceService, printService, clientService, ksefService)
                }
            }
            val response = client.post("/invoice/21")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

        coVerify(exactly = 1) { printService.sendInvoiceAgain(existingInvoice) }
        coVerify(exactly = 0) { invoiceService.createPeriodicInvoiceForClient(any(), any(), any()) }
        coVerify(exactly = 0) { ksefService.sendInvoiceToKsef(any()) }
        coVerify(exactly = 0) { printService.sendPeriodicInvoice(any()) }
    }

    @Test
    fun `POST invoice clientId returns mail error when resend fails`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val printService = mockk<PrintService>()
        val clientService = mockk<ClientService>()
        val ksefService = mockk<KsefService>()

        coEvery { clientService.findClientById(21L) } returns vatClient
        coEvery {
            invoiceService.findPeriodicDocumentForClient(21L, any(), true)
        } returns existingInvoice
        coEvery { printService.sendInvoiceAgain(existingInvoice) } throws ServerException("mail down")

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    invoiceRoutes(invoiceService, printService, clientService, ksefService)
                }
            }
            val response = client.post("/invoice/21")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("błąd ponownego wysłania maila") || body.contains("mail down"))
        }

        coVerify(exactly = 0) { invoiceService.createPeriodicInvoiceForClient(any(), any(), any()) }
    }

    @Test
    fun `POST invoice clientId creates saves and mails when no PERIODIC exists`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val printService = mockk<PrintService>(relaxUnitFun = true)
        val clientService = mockk<ClientService>()
        val ksefService = mockk<KsefService>()

        val created = existingInvoice.copy(invoiceNumber = "40/8/2026", ksefNumber = null)
        val saved = created.copy(ksefNumber = "KSeF-1")

        coEvery { clientService.findClientById(21L) } returns vatClient
        coEvery {
            invoiceService.findPeriodicDocumentForClient(21L, any(), true)
        } returns null
        coEvery {
            invoiceService.createPeriodicInvoiceForClient(vatClient, any(), any())
        } returns created
        // saveInvoiceWithOptionalKsef internals
        coEvery { invoiceService.getInvoiceByNumber(created.invoiceNumber!!) } returns null
        coEvery { invoiceService.hasPeriodicDocumentForClient(any(), any(), any()) } returns false
        coEvery { ksefService.sendInvoiceToKsef(created) } returns com.kontenery.ksef.dto.KsefSendInvoiceResponse(
            sessionReferenceNumber = "sess",
            invoiceReferenceNumber = "inv",
            ksefNumber = "KSeF-1",
            invoiceNumber = created.invoiceNumber,
            sessionStatus = null,
        )
        coEvery { invoiceService.saveInvoiceWithErrors(true, any(), any()) } returns saved
        coEvery { ksefService.persistSessionStatus(any(), any()) } returns Unit

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    invoiceRoutes(invoiceService, printService, clientService, ksefService)
                }
            }
            val response = client.post("/invoice/21?period=2026-08-01")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

        coVerify(exactly = 1) { invoiceService.createPeriodicInvoiceForClient(vatClient, any(), any()) }
        coVerify(exactly = 1) { ksefService.sendInvoiceToKsef(created) }
        coVerify(exactly = 1) { printService.sendPeriodicInvoice(saved) }
        coVerify(exactly = 0) { printService.sendInvoiceAgain(any()) }
    }

    @Test
    fun `POST forAll resends for clients that already have PERIODIC`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val printService = mockk<PrintService>()
        val clientService = mockk<ClientService>()
        val ksefService = mockk<KsefService>()

        coEvery { clientService.getFilteredClients(true) } returns listOf(vatClient)
        coEvery {
            invoiceService.findPeriodicDocumentForClient(21L, any(), true)
        } returns existingInvoice
        coEvery { printService.sendInvoiceAgain(existingInvoice) } returns InvoiceSend(
            invoiceNumber = existingInvoice.invoiceNumber,
        )

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    invoiceRoutes(invoiceService, printService, clientService, ksefService)
                }
            }
            val response = client.post("/invoice/sendInvoices/forAll?period=2026-08-01")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

        coVerify(exactly = 1) { printService.sendInvoiceAgain(existingInvoice) }
        coVerify(exactly = 0) { invoiceService.createPeriodicInvoiceForClient(any(), any(), any()) }
    }

    @Test
    fun `POST forAll returns 200 with accumulated errors when loop throws`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val printService = mockk<PrintService>()
        val clientService = mockk<ClientService>()
        val ksefService = mockk<KsefService>()

        val secondClient = vatClient.copy(id = 22L)

        coEvery { clientService.getFilteredClients(true) } returns listOf(vatClient, secondClient)
        coEvery {
            invoiceService.findPeriodicDocumentForClient(21L, any(), true)
        } returns null
        coEvery {
            invoiceService.createPeriodicInvoiceForClient(vatClient, any(), any())
        } answers {
            arg<MutableList<InvoiceErrorMessage>>(2).add(
                InvoiceErrorMessage(
                    title = "partial failure",
                    message = "client 21 skipped",
                    clientId = 21L,
                ),
            )
            null
        }
        coEvery {
            invoiceService.findPeriodicDocumentForClient(22L, any(), true)
        } throws RuntimeException("unexpected loop failure")

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    invoiceRoutes(invoiceService, printService, clientService, ksefService)
                }
            }
            val response = client.post("/invoice/sendInvoices/forAll?period=2026-08-01")

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("partial failure"))
            assertTrue(body.contains("client 21 skipped"))
        }
    }
}
