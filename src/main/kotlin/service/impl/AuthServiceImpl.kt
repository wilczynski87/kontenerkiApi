package com.kontenery.service.impl

import com.kontenery.AuthConfig
import com.kontenery.library.model.auth.ChangePasswordRequest
import com.kontenery.library.model.auth.LoginRequest
import com.kontenery.library.model.auth.LoginResponse
import com.kontenery.library.model.auth.TokenResponse
import com.kontenery.repository.ClientRepo
import com.kontenery.repository.WorkerRepo
import com.kontenery.service.AuthService
import com.kontenery.service.ChangePasswordResult
import com.kontenery.service.JwtConfig
import com.kontenery.service.TokenValidationResult
import java.util.Date

class AuthServiceImpl(
    private val jwtConfig: JwtConfig,
    authConfig: AuthConfig,
    private val clientRepo: ClientRepo,
    private val workerRepo: WorkerRepo,
): AuthService {
    val appLogin = authConfig.appLogin
    val appPassword = authConfig.appSecret

    override suspend fun login(loginRequest: LoginRequest): LoginResponse? {
        val normalizedEmail = loginRequest.email.trim().lowercase()
        val providedSecret = loginRequest.password.trim()

        if (normalizedEmail == appLogin?.trim()?.lowercase() && providedSecret == appPassword) {
            return LoginResponse("0", "admin")
        }

        val client = clientRepo.findClientByEmail(normalizedEmail)
        if (client != null) {
            val expectedSecret = client.resolvePassword() ?: return null
            return if (providedSecret == expectedSecret) {
                LoginResponse(client.id.toString(), "customer")
            } else {
                null
            }
        }

        val matchingWorkers = workerRepo.findWorkersByEmail(normalizedEmail)
            .filter { it.resolvePassword() == providedSecret }

        return when (matchingWorkers.size) {
            1 -> LoginResponse(matchingWorkers.first().id.toString(), "employee")
            else -> null
        }
    }

    override suspend fun changePassword(
        userId: String,
        request: ChangePasswordRequest,
    ): ChangePasswordResult {
        if (userId.isBlank() || userId == "0") {
            return ChangePasswordResult.Forbidden("Password change is not available for this account")
        }

        val clientId = userId.toLongOrNull()
            ?: return ChangePasswordResult.BadRequest("Invalid user id")

        val currentPassword = request.currentPassword.trim()
        val newPassword = request.newPassword.trim()

        if (currentPassword.isEmpty()) {
            return ChangePasswordResult.BadRequest("Current password is required")
        }
        if (newPassword.isEmpty()) {
            return ChangePasswordResult.BadRequest("New password is required")
        }
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            return ChangePasswordResult.BadRequest(
                "New password must be at least $MIN_PASSWORD_LENGTH characters",
            )
        }
        if (newPassword == currentPassword) {
            return ChangePasswordResult.BadRequest("New password must be different from the current password")
        }

        val client = clientRepo.findClientById(clientId)
            ?: return ChangePasswordResult.NotFound

        val expectedSecret = client.resolvePassword()
            ?: return ChangePasswordResult.InvalidCurrent

        if (currentPassword != expectedSecret) {
            return ChangePasswordResult.InvalidCurrent
        }

        clientRepo.updateClient(client.copy(password = newPassword))
            ?: return ChangePasswordResult.NotFound

        return ChangePasswordResult.Ok
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
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