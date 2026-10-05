package com.kontenery.controller

import com.kontenery.data.utils.errors.InvoiceErrorMessage
import com.kontenery.service.AppSettingsService
import com.kontenery.service.InvoiceService
import com.kontenery.utils.internalApiKeyProvider
import io.ktor.client.request.header
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock

@ResourceLock("INTERNAL_API_KEY")
class InternalInvoiceControllerTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val defaultProvider = internalApiKeyProvider

    @BeforeEach
    fun setApiKey() {
        internalApiKeyProvider = { "test-internal-key" }
    }

    @AfterEach
    fun restoreApiKey() {
        internalApiKeyProvider = defaultProvider
    }

    @Test
    fun `POST internal forAll returns 401 without X-Internal-Key`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val appSettingsService = mockk<AppSettingsService>()

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    internalInvoiceRoutes(invoiceService, appSettingsService)
                }
            }
            val response = client.post("/internal/invoice/sendInvoices/forAll")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        coVerify(exactly = 0) { invoiceService.sendPeriodicInvoicesForAll(any()) }
    }

    @Test
    fun `POST internal forAll returns 401 with wrong key`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val appSettingsService = mockk<AppSettingsService>()

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    internalInvoiceRoutes(invoiceService, appSettingsService)
                }
            }
            val response = client.post("/internal/invoice/sendInvoices/forAll") {
                header("X-Internal-Key", "wrong-key")
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        coVerify(exactly = 0) { invoiceService.sendPeriodicInvoicesForAll(any()) }
    }

    @Test
    fun `POST internal forAll skips when auto-send disabled`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val appSettingsService = mockk<AppSettingsService>()
        coEvery { appSettingsService.isPeriodicInvoiceAutoSendEnabled() } returns false

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    internalInvoiceRoutes(invoiceService, appSettingsService)
                }
            }
            val response = client.post("/internal/invoice/sendInvoices/forAll") {
                header("X-Internal-Key", "test-internal-key")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("skipped"))
            assertTrue(body.contains("periodic_auto_send_disabled"))
        }

        coVerify(exactly = 0) { invoiceService.sendPeriodicInvoicesForAll(any()) }
    }

    @Test
    fun `POST internal forAll happy path when auto-send enabled`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val appSettingsService = mockk<AppSettingsService>()
        coEvery { appSettingsService.isPeriodicInvoiceAutoSendEnabled() } returns true
        coEvery { invoiceService.sendPeriodicInvoicesForAll(any()) } returns emptyList()

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    internalInvoiceRoutes(invoiceService, appSettingsService)
                }
            }
            val response = client.post("/internal/invoice/sendInvoices/forAll?period=2026-08-01") {
                header("X-Internal-Key", "test-internal-key")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("[]", response.bodyAsText())
        }

        coVerify(exactly = 1) {
            invoiceService.sendPeriodicInvoicesForAll(LocalDate(2026, 8, 1))
        }
    }

    @Test
    fun `POST internal forAll returns service errors when enabled`() = runTest {
        val invoiceService = mockk<InvoiceService>()
        val appSettingsService = mockk<AppSettingsService>()
        coEvery { appSettingsService.isPeriodicInvoiceAutoSendEnabled() } returns true
        coEvery { invoiceService.sendPeriodicInvoicesForAll(any()) } returns listOf(
            InvoiceErrorMessage(
                title = "mail error",
                message = "failed",
                clientId = 1L,
            ),
        )

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    internalInvoiceRoutes(invoiceService, appSettingsService)
                }
            }
            val response = client.post("/internal/invoice/sendInvoices/forAll") {
                header("X-Internal-Key", "test-internal-key")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("mail error"))
        }
    }
}
