package com.kontenery.service.impl

import com.kontenery.AuthConfig
import com.kontenery.library.model.auth.LoginRequest
import com.kontenery.library.model.auth.LoginResponse
import com.kontenery.library.model.auth.TokenResponse
import com.kontenery.repository.ClientRepo
import com.kontenery.service.AuthService
import com.kontenery.service.JwtConfig
import com.kontenery.service.TokenValidationResult
import java.util.Date

class AuthServiceImpl(
    private val jwtConfig: JwtConfig,
    authConfig: AuthConfig,
    private val clientRepo: ClientRepo,
): AuthService {
    val appLogin = authConfig.appLogin
    val appPassword = authConfig.appSecret

    override suspend fun login(loginRequest: LoginRequest): LoginResponse? {
        val normalizedEmail = loginRequest.email.trim().lowercase()
        val providedSecret = loginRequest.password.trim()

        if (normalizedEmail == appLogin?.trim()?.lowercase() && providedSecret == appPassword) {
            return LoginResponse("0", "admin")
        }

        val client = clientRepo.findClientByEmail(normalizedEmail) ?: return null
        val personalData = client.clientPrivate ?: return null
        val expectedSecret = client.password?.takeUnless { it.isBlank() }
            ?: personalData.pesel?.takeUnless { it.isBlank() }
            ?: return null

        return if (providedSecret == expectedSecret) {
            LoginResponse(client.id.toString(), "customer")
        } else {
            null
        }
    }

    override fun generateTokenResponse(loginResponse: LoginResponse): TokenResponse {
        return TokenResponse(
            accessToken = jwtConfig.generateAccessToken(loginResponse.userId, loginResponse.role),
            refreshToken = jwtConfig.generateRefreshToken(loginResponse.userId, loginResponse.role),
            expiresIn = obtainExpirationDate().toInstant().nano,
            tokenType = "Bearer"
        )
    }

    override fun refresh(refreshToken: String): Boolean {
//        TODO("Not yet implemented")
        return true
    }

    override fun logout(refreshToken: String): Boolean {
//        TODO("Not yet implemented")
        return true
    }

    private fun obtainExpirationDate() = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000L)

    override fun validateRefreshToken(token: String): TokenValidationResult? {
        return try {
            jwtConfig.verifyRefreshToken(token)
        } catch (e: Exception) {
            null
        }
    }
//
//    private val refreshTokenVerifier = JWT.require(REFRESH_ALGORITHM)
//        .withIssuer("auth-server")
//        .build()
}