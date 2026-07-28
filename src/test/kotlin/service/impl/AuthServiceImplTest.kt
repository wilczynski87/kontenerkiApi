package com.kontenery.service.impl

import com.kontenery.ApiConfig
import com.kontenery.AuthConfig
import com.kontenery.DbConfig
import com.kontenery.EmailConfig
import com.kontenery.GateConfig
import com.kontenery.KsefConfig
import com.kontenery.data.Client
import com.kontenery.data.ClientPersonalData
import com.kontenery.library.model.auth.LoginRequest
import com.kontenery.library.model.auth.LoginResponse
import com.kontenery.repository.ClientRepo
import com.kontenery.service.JwtConfig
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AuthServiceImplTest {

    private lateinit var clientRepo: ClientRepo
    private lateinit var jwtConfig: JwtConfig
    private lateinit var service: AuthServiceImpl

    @BeforeEach
    fun setUp() {
        clientRepo = mockk()
        jwtConfig = JwtConfig(testApiConfig())
        service = AuthServiceImpl(
            jwtConfig = jwtConfig,
            authConfig = testApiConfig().auth,
            clientRepo = clientRepo,
        )
    }

    @Nested
    inner class Login {

        @Test
        fun `authenticates admin with app credentials`() = runTest {
            val result = service.login(LoginRequest(email = "admin@example.com", password = "admin-secret"))

            assertEquals(LoginResponse("0", "admin"), result)
            coVerify(exactly = 0) { clientRepo.findClientByEmail(any()) }
        }

        @Test
        fun `normalizes admin email case and surrounding spaces`() = runTest {
            val result = service.login(LoginRequest(email = "  Admin@Example.com  ", password = "admin-secret"))

            assertEquals(LoginResponse("0", "admin"), result)
        }

        @Test
        fun `authenticates client by password when password is set`() = runTest {
            coEvery { clientRepo.findClientByEmail("jan@example.com") } returns Client(
                id = 15,
                password = "tajne123",
                clientPrivate = ClientPersonalData(
                    email = "jan@example.com",
                    pesel = "90010112345",
                ),
            )

            val result = service.login(LoginRequest(email = "jan@example.com", password = "tajne123"))

            assertEquals(LoginResponse("15", "customer"), result)
        }

        @Test
        fun `falls back to pesel when client has no password`() = runTest {
            coEvery { clientRepo.findClientByEmail("anna@example.com") } returns Client(
                id = 27,
                password = null,
                clientPrivate = ClientPersonalData(
                    email = "anna@example.com",
                    pesel = "88050554321",
                ),
            )

            val result = service.login(LoginRequest(email = "anna@example.com", password = "88050554321"))

            assertEquals(LoginResponse("27", "customer"), result)
        }

        @Test
        fun `falls back to pesel when client password is blank`() = runTest {
            coEvery { clientRepo.findClientByEmail("anna@example.com") } returns Client(
                id = 27,
                password = "   ",
                clientPrivate = ClientPersonalData(
                    email = "anna@example.com",
                    pesel = "88050554321",
                ),
            )

            val result = service.login(LoginRequest(email = "anna@example.com", password = "88050554321"))

            assertEquals(LoginResponse("27", "customer"), result)
        }

        @Test
        fun `does not accept pesel when password is set`() = runTest {
            coEvery { clientRepo.findClientByEmail("jan@example.com") } returns Client(
                id = 15,
                password = "tajne123",
                clientPrivate = ClientPersonalData(
                    email = "jan@example.com",
                    pesel = "90010112345",
                ),
            )

            val result = service.login(LoginRequest(email = "jan@example.com", password = "90010112345"))

            assertNull(result)
        }

        @Test
        fun `rejects invalid password for existing client`() = runTest {
            coEvery { clientRepo.findClientByEmail("jan@example.com") } returns Client(
                id = 15,
                password = "tajne123",
                clientPrivate = ClientPersonalData(
                    email = "jan@example.com",
                    pesel = "90010112345",
                ),
            )

            val result = service.login(LoginRequest(email = "jan@example.com", password = "bledne"))

            assertNull(result)
        }

        @Test
        fun `returns null when client email is unknown`() = runTest {
            coEvery { clientRepo.findClientByEmail("unknown@example.com") } returns null

            val result = service.login(LoginRequest(email = "unknown@example.com", password = "anything"))

            assertNull(result)
        }

        @Test
        fun `returns null when client has no personal data`() = runTest {
            coEvery { clientRepo.findClientByEmail("firma@example.com") } returns Client(
                id = 40,
                password = "haslo",
                clientPrivate = null,
            )

            val result = service.login(LoginRequest(email = "firma@example.com", password = "haslo"))

            assertNull(result)
        }

        @Test
        fun `returns null when client has neither password nor pesel`() = runTest {
            coEvery { clientRepo.findClientByEmail("empty@example.com") } returns Client(
                id = 41,
                password = null,
                clientPrivate = ClientPersonalData(
                    email = "empty@example.com",
                    pesel = null,
                ),
            )

            val result = service.login(LoginRequest(email = "empty@example.com", password = "cokolwiek"))

            assertNull(result)
        }

        @Test
        fun `trims provided password before comparison`() = runTest {
            coEvery { clientRepo.findClientByEmail("jan@example.com") } returns Client(
                id = 15,
                password = "tajne123",
                clientPrivate = ClientPersonalData(email = "jan@example.com", pesel = "90010112345"),
            )

            val result = service.login(LoginRequest(email = "jan@example.com", password = "  tajne123  "))

            assertEquals(LoginResponse("15", "customer"), result)
        }
    }

    @Nested
    inner class GenerateTokenResponse {

        @Test
        fun `creates access and refresh tokens for customer role`() {
            val response = service.generateTokenResponse(LoginResponse("15", "customer"))

            assertEquals("Bearer", response.tokenType)
            assertNotNull(response.accessToken)
            assertNotNull(response.refreshToken)

            val access = jwtConfig.verifyAccessToken(response.accessToken)
            assertTrue(access.isValid, "access invalid: ${access.error}")
            assertEquals("15", access.userId)

            val refresh = jwtConfig.verifyRefreshToken(response.refreshToken!!)
            assertTrue(refresh.isValid, "refresh invalid: ${refresh.error}")
            assertEquals("15", refresh.userId)
            assertEquals("customer", refresh.role)
        }

        @Test
        fun `creates tokens for admin role`() {
            val response = service.generateTokenResponse(LoginResponse("0", "admin"))

            val access = jwtConfig.verifyAccessToken(response.accessToken)
            assertTrue(access.isValid, "access invalid: ${access.error}")
            assertEquals("0", access.userId)

            val refresh = jwtConfig.verifyRefreshToken(response.refreshToken!!)
            assertTrue(refresh.isValid, "refresh invalid: ${refresh.error}")
            assertEquals("admin", refresh.role)
        }
    }

    @Nested
    inner class ValidateRefreshToken {

        @Test
        fun `returns validation result for valid refresh token`() {
            val token = jwtConfig.generateRefreshToken("15", "customer")

            val result = service.validateRefreshToken(token)

            assertNotNull(result)
            assertTrue(result!!.isValid, "refresh invalid: ${result.error}")
            assertEquals("15", result.userId)
            assertEquals("customer", result.role)
        }

        @Test
        fun `returns invalid result for access token used as refresh`() {
            val accessToken = jwtConfig.generateAccessToken("15", "customer")

            val result = service.validateRefreshToken(accessToken)

            assertNotNull(result)
            assertFalse(result!!.isValid)
        }
    }

    private fun testApiConfig(): ApiConfig = ApiConfig(
        env = "TEST",
        email = EmailConfig(host = "localhost", port = 8200),
        db = DbConfig(host = "localhost", port = 5431, name = "db1", user = "user", password = "pass"),
        auth = AuthConfig(
            secretAuth = "test-access-secret-key-very-long-and-secure-12345",
            secretRefresh = "test-refresh-secret-key-very-long-and-secure-12345",
            issuer = "test-issuer",
            audience = "test-audience",
            realm = "test-realm",
            googleClientId = "google-client-id",
            appLogin = "admin@example.com",
            appSecret = "admin-secret",
        ),
        ksef = KsefConfig(
            environment = "TEST",
            baseUrl = "https://example.com",
            apiSuffix = "v2",
            token = null,
            nip = null,
        ),
        gate = GateConfig(),
    )
}
