package com.kontenery.controller

import com.kontenery.ksef.service.KsefService
import com.kontenery.service.AppSettingsService
import com.kontenery.service.ClientService
import com.kontenery.service.InvoiceService
import com.kontenery.service.PrintService
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InvoiceSettingsControllerTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `GET periodicAutoSend returns current setting`() = runTest {
        val appSettingsService = mockk<AppSettingsService>()
        coEvery { appSettingsService.isPeriodicInvoiceAutoSendEnabled() } returns true

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    invoiceRoutes(
                        mockk(),
                        mockk(),
                        mockk(),
                        mockk(),
                        appSettingsService,
                    )
                }
            }
            val response = client.get("/invoice/settings/periodicAutoSend")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("\"enabled\":true"))
        }
    }

    @Test
    fun `POST periodicAutoSend updates setting`() = runTest {
        val appSettingsService = mockk<AppSettingsService>()
        coEvery { appSettingsService.setPeriodicInvoiceAutoSendEnabled(false) } returns false

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    invoiceRoutes(
                        mockk<InvoiceService>(),
                        mockk<PrintService>(),
                        mockk<ClientService>(),
                        mockk<KsefService>(),
                        appSettingsService,
                    )
                }
            }
            val response = client.post("/invoice/settings/periodicAutoSend") {
                contentType(ContentType.Application.Json)
                setBody("""{"enabled":false}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains("\"enabled\":false"))
        }

        coVerify(exactly = 1) { appSettingsService.setPeriodicInvoiceAutoSendEnabled(false) }
    }
}
