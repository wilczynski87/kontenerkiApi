package com.kontenery.library.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthSession(val userId: String)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val userId: String,
    val role: String
)

@Serializable
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresIn: Int? = null,
    val tokenType: String = "Bearer"
)

@Serializable
data class AuthResponse(
    val loginResponse: LoginResponse,
    val tokenResponse: TokenResponse
)