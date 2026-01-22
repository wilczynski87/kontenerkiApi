package com.kontenery.service

import com.kontenery.library.model.auth.LoginRequest
import com.kontenery.library.model.auth.LoginResponse
import com.kontenery.library.model.auth.TokenResponse

interface AuthService {
    fun login(loginRequest: LoginRequest): LoginResponse?
    fun generateTokenResponse(loginResponse: LoginResponse): TokenResponse
    fun refresh(refreshToken: String): Boolean
    fun logout(refreshToken: String): Boolean
    fun validateRefreshToken(token: String): TokenValidationResult?
}