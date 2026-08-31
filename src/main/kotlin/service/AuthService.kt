package com.kontenery.service

import com.kontenery.library.model.auth.ChangePasswordRequest
import com.kontenery.library.model.auth.LoginRequest
import com.kontenery.library.model.auth.LoginResponse
import com.kontenery.library.model.auth.TokenResponse

interface AuthService {
    suspend fun login(loginRequest: LoginRequest): LoginResponse?
    suspend fun loginWithGoogle(idToken: String): GoogleLoginResult
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

sealed class GoogleLoginResult {
    data class Success(val loginResponse: LoginResponse) : GoogleLoginResult()
    data object InvalidToken : GoogleLoginResult()
    data object EmailNotVerified : GoogleLoginResult()
    data object ClientNotFound : GoogleLoginResult()
    data object ClientInactive : GoogleLoginResult()
    data object GoogleSubConflict : GoogleLoginResult()
    data object NotConfigured : GoogleLoginResult()
}