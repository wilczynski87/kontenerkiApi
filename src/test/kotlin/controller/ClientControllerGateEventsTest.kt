package com.kontenery.controller

import com.kontenery.ApiConfig
import com.kontenery.configureSecurity
import com.kontenery.configureStatusPages
import com.kontenery.data.gate.GateEventDto
import com.kontenery.service.ClientService
import com.kontenery.service.JwtConfig
import com.kontenery.service.WorkerService
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ClientControllerGateEventsTest {

    private lateinit var jwtConfig: JwtConfig
    private val json = Json { ignoreUnknownKeys = true }

    @BeforeEach
    fun setUp() {
        val apiConfig = mockk<ApiConfig> {
            every { auth.secretAuth } returns "test-access-secret-key-very-long-and-secure-12345"
            every { auth.secretRefresh } returns "test-refresh-secret-key-very-long-and-secure-12345"
            every { auth.issuer } returns "test-issuer"
            every { auth.accessTokenExpiry } returns 3_600_000L
            every { auth.refreshTokenExpiry } returns 2_592_000_000L
        }
        jwtConfig = JwtConfig(apiConfig)
    }

    @Test
    fun `GET gate-events returns events for authenticated customer`() = runTest {
        val workerService = mockk<WorkerService>()
        val events = listOf(GateEventDto(openedAtEpochMs = 1_700_000_000_000L))
        coEvery { workerService.listGateEventsForWorker(1L, 5L, 100) } returns events

        testApplication {
            application {
                install(ContentNegotiation) { json() }
                configureStatusPages()
                configureSecurity(jwtConfig)
                routing {
                    authenticate("auth-jwt") {
                        clientRoute(mockk<ClientService>(relaxed = true), workerService)
                    }
                }
            }

            val token = jwtConfig.generateAccessToken("1", "customer")
            val response = client.get("/client/employees/5/gate-events") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.decodeFromString(
                ListSerializer(GateEventDto.serializer()),
                response.bodyAsText(),
            )
            assertEquals(events, body)
            coVerify { workerService.listGateEventsForWorker(1L, 5L, 100) }
        }
    }

    @Test
    fun `GET gate-events returns 404 when worker not found`() = runTest {
        val workerService = mockk<WorkerService>()
        coEvery { workerService.listGateEventsForWorker(1L, 5L, 100) } throws
            IllegalArgumentException("Employee not found")

        testApplication {
            application {
                install(ContentNegotiation) { json() }
                configureStatusPages()
                configureSecurity(jwtConfig)
                routing {
                    authenticate("auth-jwt") {
                        clientRoute(mockk<ClientService>(relaxed = true), workerService)
                    }
                }
            }

            val token = jwtConfig.generateAccessToken("1", "customer")
            val response = client.get("/client/employees/5/gate-events") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }

    @Test
    fun `GET gate-events returns 401 without token`() = runTest {
        val workerService = mockk<WorkerService>(relaxed = true)

        testApplication {
            application {
                install(ContentNegotiation) { json() }
                configureStatusPages()
                configureSecurity(jwtConfig)
                routing {
                    authenticate("auth-jwt") {
                        clientRoute(mockk<ClientService>(relaxed = true), workerService)
                    }
                }
            }

            val response = client.get("/client/employees/5/gate-events")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    fun `GET gate-events passes custom limit query parameter`() = runTest {
        val workerService = mockk<WorkerService>()
        coEvery { workerService.listGateEventsForWorker(1L, 5L, 25) } returns emptyList()

        testApplication {
            application {
                install(ContentNegotiation) { json() }
                configureStatusPages()
                configureSecurity(jwtConfig)
                routing {
                    authenticate("auth-jwt") {
                        clientRoute(mockk<ClientService>(relaxed = true), workerService)
                    }
                }
            }

            val token = jwtConfig.generateAccessToken("1", "customer")
            val response = client.get("/client/employees/5/gate-events?limit=25") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            coVerify { workerService.listGateEventsForWorker(1L, 5L, 25) }
        }
    }
}
