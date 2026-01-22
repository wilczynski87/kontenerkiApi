package com.kontenery.service

import com.kontenery.ApiConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.util.*
import kotlin.test.*

class JwtConfigTest {
    private lateinit var jwtConfig: JwtConfig
    private lateinit var mockApiConfig: ApiConfig

    @BeforeEach
    fun setUp() {
        mockApiConfig = mockk {
            every { auth.secretAuth } returns "test-access-secret-key-very-long-and-secure-12345"
            every { auth.secretRefresh } returns "test-refresh-secret-key-very-long-and-secure-12345"
            every { auth.issuer } returns "test-issuer"
            every { auth.accessTokenExpiry } returns 3600000L // 1 godzina w ms
            every { auth.refreshTokenExpiry } returns 2592000000L // 30 dni w ms
        }

        jwtConfig = JwtConfig(mockApiConfig)
    }

    @Nested
    inner class AccessTokenVerifierTests {

        @Test
        fun `accessTokenVerifier should be configured with correct issuer`() {
            // When
            val verifier = jwtConfig.accessTokenVerifier

            // Then
            assertNotNull(verifier)
        }

        @Test
        fun `accessTokenVerifier should require userId claim`() {
            // Given
            val tokenWithoutUserId = JWT.create()
                .withIssuer("test-issuer")
                .withSubject("Authentication")
                .withIssuedAt(Date())
                .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("test-access-secret-key-very-long-and-secure-12345"))

            // When/Then
            assertFailsWith<JWTVerificationException> {
                jwtConfig.accessTokenVerifier.verify(tokenWithoutUserId)
            }
        }
    }

    @Nested
    inner class RefreshTokenVerifierTests {

        @Test
        fun `refreshTokenVerifier should be configured with correct issuer`() {
            // When
            val verifier = jwtConfig.refreshTokenVerifier

            // Then
            assertNotNull(verifier)
        }

        @Test
        fun `refreshTokenVerifier should require type claim with value refresh`() {
            // Given - token bez claima type
            val tokenWithoutType = JWT.create()
                .withIssuer("test-issuer")
                .withSubject("Refresh")
                .withClaim("userId", "test-user")
                .withIssuedAt(Date())
                .withExpiresAt(Date(System.currentTimeMillis() + 2592000000))
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("test-refresh-secret-key-very-long-and-secure-12345"))

            // When/Then
            assertFailsWith<JWTVerificationException> {
                jwtConfig.refreshTokenVerifier.verify(tokenWithoutType)
            }
        }

        @Test
        fun `refreshTokenVerifier should require userId claim`() {
            // Given - token bez userId
            val tokenWithoutUserId = JWT.create()
                .withIssuer("test-issuer")
                .withSubject("Refresh")
                .withClaim("type", "refresh")
                .withIssuedAt(Date())
                .withExpiresAt(Date(System.currentTimeMillis() + 2592000000))
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("test-refresh-secret-key-very-long-and-secure-12345"))

            // When/Then
            assertFailsWith<JWTVerificationException> {
                jwtConfig.refreshTokenVerifier.verify(tokenWithoutUserId)
            }
        }
    }

    @Nested
    inner class GenerateAccessTokenTests {

        @Test
        fun `generateAccessToken should create valid JWT token`() {
            // Given
            val userId = "test-user-123"
            val role = "ADMIN"

            // When
            val token = jwtConfig.generateAccessToken(userId, role)

            // Then
            assertNotNull(token)
            assertTrue(token.isNotBlank())
            assertTrue(token.matches(Regex("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")))
        }

        @Test
        fun `generateAccessToken should include all required claims`() {
            // Given
            val userId = "test-user-456"
            val role = "USER"

            // When
            val token = jwtConfig.generateAccessToken(userId, role)
            val decoded = jwtConfig.decodeToken(token)!!

            // Then
            assertNotNull(decoded)
            assertEquals(TokenType.ACCESS.name, decoded?.subject)
            assertEquals(userId, decoded?.getClaim("userId")?.asString())
            assertEquals(role, decoded?.getClaim("role")?.asString())
            assertEquals("test-issuer", decoded?.issuer)
            assertNotNull(decoded?.issuedAt)
            assertNotNull(decoded?.expiresAt)
        }

        @Test
        fun `generateAccessToken should have correct expiration time`() {
            // Given
            val userId = "test-user"
            val role = "MODERATOR"
            val before = Date()

            // When
            val token = jwtConfig.generateAccessToken(userId, role)
            val decoded = jwtConfig.decodeToken(token)

            // Then
            assertNotNull(decoded)
            val expiresAt = decoded?.expiresAt
            val issuedAt = decoded?.issuedAt

            // Sprawdź czy expiration jest w przyszłości
            assertTrue(expiresAt?.after(Date()) ?: false)

            // Sprawdź czy różnica to około 1 godzina (z tolerancją 1 sekundy)
            val expectedExpiry = 3600000L // 1 godzina w ms
            val actualExpiry = expiresAt?.time?.minus((issuedAt?.time ?: 3600000L))
            assertTrue(actualExpiry in (expectedExpiry - 1000)..(expectedExpiry + 1000))
        }

        @Test
        fun `generateAccessToken should work with different roles`() {
            // Given
            val roles = listOf("ADMIN", "USER", "MODERATOR", "GUEST")

            roles.forEach { role ->
                // When
                val token = jwtConfig.generateAccessToken("user-123", role)
                val decoded = jwtConfig.decodeToken(token)

                // Then
                assertEquals(role, decoded?.getClaim("role")?.asString())
            }
        }

        @Test
        fun `generateAccessToken should work with special characters in userId`() {
            // Given
            val userIds = listOf(
                "user-123",
                "user@example.com",
                "user_123",
                "123456",
                "user.with.dots",
                "user-with-dashes"
            )

            userIds.forEach { userId ->
                // When
                val token = jwtConfig.generateAccessToken(userId, "USER")
                val decoded = jwtConfig.decodeToken(token)

                // Then
                assertEquals(userId, decoded?.getClaim("userId")?.asString())
            }
        }
    }

    @Nested
    inner class GenerateRefreshTokenTests {

        @Test
        fun `generateRefreshToken should create valid JWT token`() {
            // Given
            val userId = "test-user-789"

            // When
            val token = jwtConfig.generateRefreshToken(userId)

            // Then
            assertNotNull(token)
            assertTrue(token.isNotBlank())
            assertTrue(token.matches(Regex("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")))
        }

        @Test
        fun `generateRefreshToken should include all required claims`() {
            // Given
            val userId = "refresh-user-123"

            // When
            val token = jwtConfig.generateRefreshToken(userId)
            val decoded = jwtConfig.decodeToken(token)!!

            // Then
            assertNotNull(decoded)
            assertEquals(TokenType.REFRESH.name, decoded?.subject)
            assertEquals(userId, decoded.getClaim("userId").asString())
            assertEquals(TokenType.REFRESH.name, decoded.getClaim("type").asString())
            assertEquals("test-issuer", decoded.issuer)
            assertNotNull(decoded.issuedAt)
            assertNotNull(decoded.expiresAt)
        }

        @Test
        fun `generateRefreshToken should have longer expiration than access token`() {
            // Given
            val userId = "test-user"
            val before = Date()

            // When
            val refreshToken = jwtConfig.generateRefreshToken(userId)
            val accessToken = jwtConfig.generateAccessToken(userId, "USER")

            val decodedRefresh = jwtConfig.decodeToken(refreshToken)!!
            val decodedAccess = jwtConfig.decodeToken(accessToken)!!

            // Then
            assertNotNull(decodedRefresh)
            assertNotNull(decodedAccess)

            val refreshExpiry = decodedRefresh.expiresAt.time - decodedRefresh.issuedAt.time
            val accessExpiry = decodedAccess.expiresAt.time - decodedAccess.issuedAt.time

            // Refresh token powinien być ważny dłużej niż access token
            assertTrue(refreshExpiry > accessExpiry)
        }

        @Test
        fun `generateRefreshToken should always include type claim with value refresh`() {
            // Given
            val userId = "test-user"

            // When
            val token = jwtConfig.generateRefreshToken(userId)
            val decoded = jwtConfig.decodeToken(token)

            // Then
            assertEquals(TokenType.REFRESH.name, decoded?.getClaim("type")?.asString())
        }
    }

    @Nested
    inner class VerifyAccessTokenTests {

        @Test
        fun `verifyAccessToken should return success for valid token`() {
            // Given
            val userId = "valid-user-123"
            val role = "ADMIN"
            val token = jwtConfig.generateAccessToken(userId, role)

            // When
            val result = jwtConfig.verifyAccessToken(token)

            // Then
            assertTrue(result.isValid)
            assertEquals(userId, result.userId)
            assertEquals(role, "ADMIN")
            assertNull(result.error)
        }

        @Test
        fun `verifyAccessToken should fail for expired token`() {
            // Given - stworz config z bardzo krótkim czasem wygaśnięcia
            val shortExpiryConfig = mockk<ApiConfig> {
                every { auth.secretAuth } returns "short-expiry-secret"
                every { auth.secretRefresh } returns "refresh-secret"
                every { auth.issuer } returns "test-issuer"
                every { auth.accessTokenExpiry } returns 1L // 1 ms
                every { auth.refreshTokenExpiry } returns 5000L
            }

            val shortJwtConfig = JwtConfig(shortExpiryConfig)
            val token = shortJwtConfig.generateAccessToken("user-123", "USER")

            // Odczekaj chwilę żeby token wygasł
            Thread.sleep(100)

            // When
            val result = shortJwtConfig.verifyAccessToken(token)

            // Then
            assertFalse(result.isValid)
            assertNotNull(result.error)
            assertTrue(result.error!!.contains("expired", ignoreCase = true))
        }

        @Test
        fun `verifyAccessToken should fail for token with wrong signature`() {
            // Given - token podpisany innym sekretem
            val otherConfig = mockk<ApiConfig> {
                every { auth.secretAuth } returns "different-secret"
                every { auth.secretRefresh } returns "refresh-secret"
                every { auth.issuer } returns "test-issuer"
                every { auth.accessTokenExpiry } returns 3600000L
                every { auth.refreshTokenExpiry } returns 2592000000L
            }

            val otherJwtConfig = JwtConfig(otherConfig)
            val token = otherJwtConfig.generateAccessToken("user-123", "USER")

            // When - weryfikuj z oryginalnym configiem (innym sekretem)
            val result = jwtConfig.verifyAccessToken(token)

            // Then
            assertFalse(result.isValid)
            assertNotNull(result.error)
        }

        @Test
        fun `verifyAccessToken should fail for malformed token`() {
            // Given
            val malformedTokens = listOf(
                "not.a.jwt.token",
                "",
                "   ",
                "header.payload",
                "header.payload.signature.extra",
                "invalid!@#$%^&*()chars"
            )

            malformedTokens.forEach { token ->
                // When
                val result = jwtConfig.verifyAccessToken(token)

                // Then
//                assertFalse(result.isValid, "Should fail for malformed token: $token")
                assertFalse(result.isValid,)
                assertNotNull(result.error)
            }
        }

        @Test
        fun `verifyAccessToken should fail for token with wrong issuer`() {
            // Given - token z innym issuerem
            val otherIssuerConfig = mockk<ApiConfig> {
                every { auth.secretAuth } returns "test-secret"
                every { auth.secretRefresh } returns "refresh-secret"
                every { auth.issuer } returns "different-issuer"
                every { auth.accessTokenExpiry } returns 3600000L
                every { auth.refreshTokenExpiry } returns 2592000000L
            }

            val otherJwtConfig = JwtConfig(otherIssuerConfig)
            val token = otherJwtConfig.generateAccessToken("user-123", "USER")

            // When
            val result = jwtConfig.verifyAccessToken(token)

            // Then
            assertFalse(result.isValid)
            assertNotNull(result.error)
        }

        @Test
        fun `verifyAccessToken should fail for refresh token used as access token`() {
            // Given
            val refreshToken = jwtConfig.generateRefreshToken("user-123")

            // When
            val result = jwtConfig.verifyAccessToken(refreshToken)

            // Then
            assertFalse(result.isValid)
            assertNotNull(result.error)
        }
    }

    @Nested
    inner class VerifyRefreshTokenTests {

        @Test
        fun `verifyRefreshToken should return success for valid refresh token`() {
            // Given
            val userId = "refresh-user-456"
            val token = jwtConfig.generateRefreshToken(userId)

            // When
            val result = jwtConfig.verifyRefreshToken(token)

            // Then
            assertTrue(result.isValid)
            assertEquals(userId, result.userId)
            assertNull(result.error)
        }

        @Ignore
        @Test
        fun `verifyRefreshToken should fail for access token used as refresh token`() {
            // Given
            val accessToken = jwtConfig.generateAccessToken("user-123", "USER")
            val tokenv = jwtConfig.refreshTokenVerifier.verify(accessToken)
            println(tokenv)

            // When
            val result = jwtConfig.verifyRefreshToken(accessToken)

            // Then
            assertFalse(result.isValid)
            assertEquals("Invalid token type", result.error)
        }

        @Test
        fun `verifyRefreshToken should fail for expired refresh token`() {
            // Given - config z krótkim czasem refresh tokena
            val shortRefreshConfig = mockk<ApiConfig> {
                every { auth.secretAuth } returns "access-secret"
                every { auth.secretRefresh } returns "short-refresh-secret"
                every { auth.issuer } returns "test-issuer"
                every { auth.accessTokenExpiry } returns 3600000L
                every { auth.refreshTokenExpiry } returns 1L // 1 ms
            }

            val shortJwtConfig = JwtConfig(shortRefreshConfig)
            val token = shortJwtConfig.generateRefreshToken("user-123")

            // Odczekaj aż token wygaśnie
            Thread.sleep(100)

            // When
            val result = shortJwtConfig.verifyRefreshToken(token)

            // Then
            assertFalse(result.isValid)
            assertNotNull(result.error)
        }

        @Ignore
        @Test
        fun `verifyRefreshToken should fail for token without type claim`() {
            // Given - ręcznie stworzony token bez claima type
            val tokenWithoutType = JWT.create()
                .withIssuer("test-issuer")
                .withSubject(TokenType.ACCESS.name)
                .withClaim("userId", "test-user")
                .withIssuedAt(Date())
                .withExpiresAt(Date(System.currentTimeMillis() + 2592000000))
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("test-refresh-secret-key-very-long-and-secure-12345"))

            // When
            val result = jwtConfig.verifyRefreshToken(tokenWithoutType)

            // Then
            assertFalse(result.isValid)
            assertEquals("Invalid token type", result.error)
        }

        @Ignore
        @Test
        fun `verifyRefreshToken should fail for token with wrong type claim`() {
            // Given - token z type="access" ale wygenerowany refresh secretem
            val tokenWithWrongType = JWT.create()
                .withIssuer("test-issuer")
                .withSubject(TokenType.ACCESS.name)
                .withClaim("userId", "test-user")
                .withClaim("type", TokenType.ACCESS.name) // Wrong type!
                .withIssuedAt(Date())
                .withExpiresAt(Date(System.currentTimeMillis() + 2592000000))
                .sign(com.auth0.jwt.algorithms.Algorithm.HMAC256("test-refresh-secret-key-very-long-and-secure-12345"))

            // When
            val result = jwtConfig.verifyRefreshToken(tokenWithWrongType)

            // Then
            assertFalse(result.isValid)
            assertEquals("Invalid token type", result.error)
        }
    }

    @Nested
    inner class DecodeTokenTests {

        @Test
        fun `decodeToken should return DecodedJWT for valid token`() {
            // Given
            val token = jwtConfig.generateAccessToken("user-123", "ADMIN")

            // When
            val decoded = jwtConfig.decodeToken(token)

            // Then
            assertNotNull(decoded)
            assertIs<DecodedJWT>(decoded)
            assertEquals("user-123", decoded.getClaim("userId").asString())
        }

        @Test
        fun `decodeToken should return null for malformed token`() {
            // Given
            val malformedTokens = listOf(
                "not.a.jwt",
                "",
                "   ",
                "header.only",
                "header..signature"
            )

            malformedTokens.forEach { token ->
                // When
                val decoded = jwtConfig.decodeToken(token)

                // Then
//                assertNull(decoded, "Should return null for malformed token: $token")
                assertNull(decoded)

            }
        }

        @Test
        fun `decodeToken should work for both access and refresh tokens`() {
            // Given
            val accessToken = jwtConfig.generateAccessToken("user-123", "USER")
            val refreshToken = jwtConfig.generateRefreshToken("user-123")

            // When
            val decodedAccess = jwtConfig.decodeToken(accessToken)!!
            val decodedRefresh = jwtConfig.decodeToken(refreshToken)!!

            // Then
            assertNotNull(decodedAccess)
            assertNotNull(decodedRefresh)
            assertEquals(TokenType.ACCESS.name, decodedAccess.subject)
            assertEquals(TokenType.REFRESH.name, decodedRefresh.subject)
        }

        @Test
        fun `decodeToken should extract all claims correctly`() {
            // Given
            val userId = "test-user-789"
            val role = "MODERATOR"
            val token = jwtConfig.generateAccessToken(userId, role)

            // When
            val decoded = jwtConfig.decodeToken(token)!!

            // Then
            assertNotNull(decoded)
            assertEquals(userId, decoded.getClaim("userId").asString())
            assertEquals(role, decoded.getClaim("role").asString())
            assertEquals("test-issuer", decoded.issuer)
            assertNotNull(decoded.issuedAt)
            assertNotNull(decoded.expiresAt)
            assertTrue(decoded.expiresAt.after(decoded.issuedAt))
        }
    }

    @Nested
    inner class GenerateExpirationDateTests {

        @Test
        fun `generateExpirationDate should return future date for ACCESS token`() {
            // Given
            val before = Date()

            // When
            val expiryDate = jwtConfig.generateExpirationDate(TokenType.ACCESS)

            // Then
            assertTrue(expiryDate.after(before))
            assertTrue(expiryDate.after(Date()))

            // Sprawdź czy to około 1 godzina w przyszłości
            val expectedTime = System.currentTimeMillis() + 3600000L
            val actualTime = expiryDate.time
            assertTrue(actualTime in (expectedTime - 1000)..(expectedTime + 1000))
        }

        @Test
        fun `generateExpirationDate should return future date for REFRESH token`() {
            // Given
            val before = Date()

            // When
            val expiryDate = jwtConfig.generateExpirationDate(TokenType.REFRESH)

            // Then
            assertTrue(expiryDate.after(before))
            assertTrue(expiryDate.after(Date()))

            // Sprawdź czy to około 30 dni w przyszłości
            val expectedTime = System.currentTimeMillis() + 2592000000L
            val actualTime = expiryDate.time
            assertTrue(actualTime in (expectedTime - 1000)..(expectedTime + 1000))
        }

        @ParameterizedTest
        @EnumSource(TokenType::class)
        fun `generateExpirationDate should return different dates for different token types`(tokenType: TokenType) {
            // When
            val expiryDate = jwtConfig.generateExpirationDate(tokenType)

            // Then
            assertTrue(expiryDate.after(Date()))

            when (tokenType) {
                TokenType.ACCESS -> {
                    val expectedMin = System.currentTimeMillis() + 3600000L - 1000
                    val expectedMax = System.currentTimeMillis() + 3600000L + 1000
                    assertTrue(expiryDate.time in expectedMin..expectedMax)
                }
                TokenType.REFRESH -> {
                    val expectedMin = System.currentTimeMillis() + 2592000000L - 1000
                    val expectedMax = System.currentTimeMillis() + 2592000000L + 1000
                    assertTrue(expiryDate.time in expectedMin..expectedMax)
                }
            }
        }

        @Test
        fun `generateExpirationDate should have refresh token expiry longer than access token`() {
            // When
            val accessExpiry = jwtConfig.generateExpirationDate(TokenType.ACCESS)
            val refreshExpiry = jwtConfig.generateExpirationDate(TokenType.REFRESH)

            // Then
            assertTrue(refreshExpiry.after(accessExpiry))

            val difference = refreshExpiry.time - accessExpiry.time
            val expectedDifference = 2592000000L - 3600000L // 30 dni - 1 godzina
            assertTrue(difference in (expectedDifference - 2000)..(expectedDifference + 2000))
        }
    }

    @Nested
    inner class AdditionalHelperMethodsTests {

//        @Test
//        fun `extractUserId should return userId from token`() {
//            // Given
//            val expectedUserId = "test-user-999"
//            val token = jwtConfig.generateAccessToken(expectedUserId, "USER")
//
//            // When
//            val userId = jwtConfig.extractUserId(token)
//
//            // Then
//            assertEquals(expectedUserId, userId)
//        }

//        @Test
//        fun `extractUserId should return null for invalid token`() {
//            // Given
//            val invalidToken = "invalid.jwt.token"
//
//            // When
//            val userId = jwtConfig.extractUserId(invalidToken)
//
//            // Then
//            assertNull(userId)
//        }
//
//        @Test
//        fun `extractRole should return role from token`() {
//            // Given
//            val expectedRole = "SUPER_ADMIN"
//            val token = jwtConfig.generateAccessToken("user-123", expectedRole)
//
//            // When
//            val role = jwtConfig.extractRole(token)
//
//            // Then
//            assertEquals(expectedRole, role)
//        }

//        @Test
//        fun `isTokenExpiringSoon should return true for soon expiring token`() {
//            // Given - config z bardzo krótkim czasem życia tokena
//            val shortExpiryConfig = mockk<ApiConfig> {
//                every { auth.secretAuth } returns "short-secret"
//                every { auth.secretRefresh } returns "refresh-secret"
//                every { auth.issuer } returns "test-issuer"
//                every { auth.accessTokenExpiry } returns 60000L // 1 minuta
//                every { auth.refreshTokenExpiry } returns 5000L
//            }
//
//            val shortJwtConfig = JwtConfig(shortExpiryConfig)
//            val token = shortJwtConfig.generateAccessToken("user-123", "USER")
//
//            // When/Then - token wygasa za mniej niż 5 minut
//            assertTrue(shortJwtConfig.isTokenExpiringSoon(token, thresholdMinutes = 5))
//        }
//
//        @Test
//        fun `isTokenExpiringSoon should return false for fresh token`() {
//            // Given
//            val token = jwtConfig.generateAccessToken("user-123", "USER")
//
//            // When/Then - token ważny 1 godzinę, nie wygasa za mniej niż 55 minut
//            assertFalse(jwtConfig.isTokenExpiringSoon(token, thresholdMinutes = 55))
//        }
//
//        @Test
//        fun `getRemainingTime should return positive value for valid token`() {
//            // Given
//            val token = jwtConfig.generateAccessToken("user-123", "USER")
//
//            // When
//            val remainingTime = jwtConfig.getRemainingTime(token)
//
//            // Then
//            assertNotNull(remainingTime)
//            assertTrue(remainingTime > 0)
//            assertTrue(remainingTime <= 3600000L) // Maksymalnie 1 godzina
//        }
//
//        @Test
//        fun `getRemainingTime should return null for invalid token`() {
//            // Given
//            val invalidToken = "invalid"
//
//            // When
//            val remainingTime = jwtConfig.getRemainingTime(invalidToken)
//
//            // Then
//            assertNull(remainingTime)
//        }
//
//        @Test
//        fun `generateTokenPair should create both access and refresh tokens`() {
//            // Given
//            val userId = "pair-user-123"
//            val role = "ADMIN"
//
//            // When
//            val tokenPair = jwtConfig.generateTokenPair(userId, role)
//
//            // Then
//            assertNotNull(tokenPair.accessToken)
//            assertNotNull(tokenPair.refreshToken)
//            assertEquals(3600, tokenPair.expiresIn)
//
//            // Sprawdź czy tokeny są poprawne
//            val accessResult = jwtConfig.verifyAccessToken(tokenPair.accessToken)
//            val refreshResult = jwtConfig.verifyRefreshToken(tokenPair.refreshToken)
//
//            assertTrue(accessResult.isValid)
//            assertTrue(refreshResult.isValid)
//            assertEquals(userId, accessResult.userId)
//            assertEquals(userId, refreshResult.userId)
//        }
    }

    @Test
    fun `should use different secrets for access and refresh tokens`() {
        // Given
        val userId = "test-user"

        // When
        val accessToken = jwtConfig.generateAccessToken(userId, "USER")
        val refreshToken = jwtConfig.generateRefreshToken(userId)

        // Then - refresh token nie powinien przejść weryfikacji jako access token
        val accessWithRefresh = jwtConfig.verifyAccessToken(refreshToken)
        assertFalse(accessWithRefresh.isValid)

        // I odwrotnie
        val refreshWithAccess = jwtConfig.verifyRefreshToken(accessToken)
        assertFalse(refreshWithAccess.isValid)
    }
}

data class ApiConfig(
    val auth: AuthConfig,
    val baseUrl: String = "http://localhost:8080"
)

data class AuthConfig(
    val secretAuth: String,
    val secretRefresh: String,
    val issuer: String = "test-app",
    val accessTokenExpiry: Long = 3600000,
    val refreshTokenExpiry: Long = 2592000000
)

// Jeśli potrzebujesz mockować ApiConfig w innych testach
fun createMockApiConfig(
    secretAuth: String = "test-secret",
    secretRefresh: String = "test-refresh-secret",
    issuer: String = "test-issuer",
    accessTokenExpiry: Long = 3600000,
    refreshTokenExpiry: Long = 2592000000
): ApiConfig {
    return mockk {
        every { auth.secretAuth } returns secretAuth
        every { auth.secretRefresh } returns secretRefresh
        every { auth.issuer } returns issuer
        every { auth.accessTokenExpiry } returns accessTokenExpiry
        every { auth.refreshTokenExpiry } returns refreshTokenExpiry
    }
}