package com.kontenery.service.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kontenery.ApiConfig
import com.kontenery.AuthConfig
import com.kontenery.library.model.auth.LoginRequest
import com.kontenery.library.model.auth.LoginResponse
import com.kontenery.library.model.auth.TokenResponse
import com.kontenery.service.AuthService
import com.kontenery.service.JwtConfig
import com.kontenery.service.TokenValidationResult
import java.util.Date

class AuthServiceImpl(private val jwtConfig: JwtConfig, authConfig: AuthConfig): AuthService {
    val appLogin = authConfig.appLogin
    val appPassword = authConfig.appSecret

    override fun login(loginRequest: LoginRequest): LoginResponse? {
//        TODO("Not yet implemented")
        return if(loginRequest.email == appLogin && loginRequest.password == appPassword)
            LoginResponse("0", "admin")
        else null
    }

    override fun generateTokenResponse(loginResponse: LoginResponse): TokenResponse {
        return TokenResponse(
            accessToken = jwtConfig.generateAccessToken(loginResponse.userId, loginResponse.role),
            refreshToken = jwtConfig.generateRefreshToken(loginResponse.userId),
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