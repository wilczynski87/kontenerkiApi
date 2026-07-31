package com.kontenery.service

import com.kontenery.library.model.auth.ChangePasswordRequest
import com.kontenery.library.model.auth.LoginRequest
import com.kontenery.library.model.auth.LoginResponse
import com.kontenery.library.model.auth.TokenResponse

interface AuthService {
    suspend fun login(loginRequest: LoginRequest): LoginResponse?
    fun generateTokenResponse(loginResponse: LoginResponse): TokenResponse
    fun refresh(refreshToken: String): Boolean
    fun logout(refreshToken: String): Boolean
    fun validateRefreshToken(token: String): TokenValidationResult?
    suspend fun changePassword(userId: String, request: ChangePasswordRequest): ChangePasswordResult
}

sealed class ChangePasswordResult {
    data object Ok : ChangePasswordResult()
    data object InvalidCurrent : ChangePasswordResult()
    data object NotFound : ChangePasswordResult()
    data class Forbidden(val message: String) : ChangePasswordResult()
    data class BadRequest(val message: String) : ChangePasswordResult()
}