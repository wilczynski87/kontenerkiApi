package com.kontenery.service.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.kontenery.ApiConfig
import com.kontenery.library.model.auth.LoginRequest
import com.kontenery.library.model.auth.LoginResponse
import com.kontenery.library.model.auth.TokenResponse
import com.kontenery.service.AuthService
import java.util.Date

class AuthServiceImpl(private val apiConfig: ApiConfig): AuthService {
    override fun login(loginRequest: LoginRequest): LoginResponse? {
//        TODO("Not yet implemented")
        return if(loginRequest.email == "ppp" && loginRequest.password == "ppp")
            LoginResponse("0", "admin")
        else null
    }

    override fun generateTokenResponse(loginResponse: LoginResponse): TokenResponse {
        return TokenResponse(
            accessToken = generateJWT(loginResponse),
            refreshToken = null,
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

    private fun generateJWT(loginResponse: LoginResponse): String = JWT.create()
        .withAudience(apiConfig.auth.audience)
        .withIssuer(apiConfig.auth.issuer)
        .withClaim("role", loginResponse.role)
        .withExpiresAt(obtainExpirationDate())
        .sign(Algorithm.HMAC256(apiConfig.auth.secret))

    private fun obtainExpirationDate() = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000L)
}