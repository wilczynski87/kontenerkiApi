package com.kontenery.controller

import com.kontenery.library.model.auth.*
import com.kontenery.service.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sessions.SameSite
import io.ktor.http.Cookie

fun Route.authController(
    authService: AuthService
) {
    route("/auth") {
        post("/login") {
            try {
                val credentials: LoginRequest = call.receive<LoginRequest>()
                println("Credentials: $credentials")

                val loginResponse: LoginResponse = authService.login(credentials) ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    "Login failed"
                )

                val tokenResponse = authService.generateTokenResponse(loginResponse)

                // Zwróć tokeny w response body (nie w cookies!)
                call.respond(HttpStatusCode.OK, AuthResponse(
                    loginResponse = loginResponse,
                    tokenResponse = tokenResponse,
                    )
                )

                call.respond(HttpStatusCode.OK, loginResponse)
            } catch (e: Exception) {
                println("Exception in /auth/login")
                call.respond(HttpStatusCode.ExpectationFailed, "Login failed")
            }
        }
        authenticate("auth-jwt") {
            post("/refresh") {
                try {
//                    val refreshRequest = call.receive<RefreshRequest>()
//                    // Waliduj refresh token i wygeneruj nowy access token
//                    val newTokens = authService.refreshTokens(refreshRequest.refreshToken)
//                    call.respond(HttpStatusCode.OK, newTokens)
                } catch (e: Exception) {
                    println("Exception in /auth/refresh")
                    call.respond(HttpStatusCode.Unauthorized, "Invalid refresh token")
                }
            }

            post("/logout") {
                try {

                } catch (e: Exception) {
                    println("Exception in /auth/logout")
                }
            }
        }
    }
}