package com.kontenery.controller

import com.kontenery.configureStatusPages
import com.kontenery.data.Client
import com.kontenery.data.ClientPersonalData
import com.kontenery.service.ClientService
import com.kontenery.service.WorkerService
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClientControllerUpdateTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `PUT client uses path id when body id is null`() = runTest {
        val clientService = mockk<ClientService>()
        val workerService = mockk<WorkerService>()
        val requestBody = Client(
            clientPrivate = ClientPersonalData(firstName = "Jan", email = "jan@example.com"),
            isActive = true,
        )
        val updated = requestBody.copy(id = 5L)

        coEvery { clientService.updateClient(match { it.id == 5L }) } returns updated

        testApplication {
            application {
                install(ContentNegotiation) { json() }
                configureStatusPages()
                routing { clientRoute(clientService, workerService) }
            }

            val response = client.put("/client/5") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(json.encodeToString(requestBody))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            coVerify { clientService.updateClient(match { it.id == 5L }) }
        }
    }

    @Test
    fun `PUT client returns 400 when body id does not match path id`() = runTest {
        val clientService = mockk<ClientService>()
        val workerService = mockk<WorkerService>()
        val requestBody = Client(
            id = 99L,
            clientPrivate = ClientPersonalData(firstName = "Jan"),
        )

        testApplication {
            application {
                install(ContentNegotiation) { json() }
                configureStatusPages()
                routing { clientRoute(clientService, workerService) }
            }

            val response = client.put("/client/5") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(json.encodeToString(requestBody))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            coVerify(exactly = 0) { clientService.updateClient(any()) }
        }
    }

    @Test
    fun `PUT client returns 404 when client not found`() = runTest {
        val clientService = mockk<ClientService>()
        val workerService = mockk<WorkerService>()
        val requestBody = Client(
            id = 5L,
            clientPrivate = ClientPersonalData(firstName = "Jan"),
        )

        coEvery { clientService.updateClient(any()) } returns null

        testApplication {
            application {
                install(ContentNegotiation) { json() }
                configureStatusPages()
                routing { clientRoute(clientService, workerService) }
            }

            val response = client.put("/client/5") {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(json.encodeToString(requestBody))
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }
}
