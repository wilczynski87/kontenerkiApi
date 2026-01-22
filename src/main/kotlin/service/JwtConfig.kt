package com.kontenery.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import com.kontenery.ApiConfig
import kotlinx.serialization.Serializable
import java.util.Date

class JwtConfig(private val apiConfig: ApiConfig) {
    private val algorithm = Algorithm.HMAC256(apiConfig.auth.secretAuth)
    private val refreshAlgorithm = Algorithm.HMAC256(apiConfig.auth.secretRefresh)

    val accessTokenVerifier: JWTVerifier = JWT.require(algorithm)
        .withIssuer(apiConfig.auth.issuer)
        .withClaimPresence("userId")
        .build()

    val refreshTokenVerifier: JWTVerifier = JWT.require(refreshAlgorithm)
        .withIssuer(apiConfig.auth.issuer)
        .withClaimPresence("userId")
        .withClaim("type", TokenType.REFRESH.name)
        .build()

    fun generateAccessToken(userId: String, role: String): String {
        return JWT.create()
            .withIssuer(apiConfig.auth.issuer)
            .withSubject(TokenType.ACCESS.name)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withIssuedAt(Date())
            .withExpiresAt(generateExpirationDate(TokenType.ACCESS)) // 1 godzina
            .sign(algorithm)
    }

    fun generateRefreshToken(userId: String): String {
        return JWT.create()
            .withIssuer(apiConfig.auth.issuer)
            .withSubject(TokenType.REFRESH.name)
            .withClaim("userId", userId)
            .withClaim("type", TokenType.REFRESH.name)
            .withIssuedAt(Date())
            .withExpiresAt(generateExpirationDate(TokenType.REFRESH)) // 30 dni
            .sign(refreshAlgorithm)
    }

    fun verifyAccessToken(token: String): TokenValidationResult {
        return try {
            val decodedJWT = accessTokenVerifier.verify(token)
            TokenValidationResult(
                isValid = true,
                userId = decodedJWT.getClaim("userId").asString()
            )
        } catch (ex: JWTVerificationException) {
            TokenValidationResult(
                isValid = false,
                error = ex.message
            )
        }
    }

    fun verifyRefreshToken(token: String): TokenValidationResult {
        return try {
            val decodedJWT = refreshTokenVerifier.verify(token)
            // Dodatkowa walidacja business logic
            val tokenType = decodedJWT.getClaim("type").asString()
            println("tokenType: $tokenType")
            if (tokenType != TokenType.REFRESH.name) {
                return TokenValidationResult(false, error = "Invalid token type")
            }

            TokenValidationResult(
                isValid = true,
                userId = decodedJWT.getClaim("userId").asString()
            )
        } catch (ex: JWTVerificationException) {
            TokenValidationResult(
                isValid = false,
                error = ex.message
            )
        }
    }

    // Dekodowanie bez weryfikacji (tylko do odczytu claims)
    fun decodeToken(token: String): DecodedJWT? {
        return try {
            JWT.decode(token)
        } catch (ex: Exception) {
            null
        }
    }

    fun generateExpirationDate(tokenType: TokenType): Date =
        if (tokenType == TokenType.REFRESH) {
            Date(System.currentTimeMillis() + apiConfig.auth.refreshTokenExpiry)
        } else {
            Date(System.currentTimeMillis() + apiConfig.auth.accessTokenExpiry)
        }

}

@Serializable
data class TokenValidationResult(
    val isValid: Boolean,
    val userId: String? = null,
    val error: String? = null
)

enum class TokenType {
    ACCESS, REFRESH
}

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)