package com.kontenery.controller

import com.kontenery.library.model.auth.*
import com.kontenery.service.AuthService
import com.kontenery.service.RefreshTokenRequest
import com.kontenery.service.TokenType
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sessions.SameSite
import io.ktor.http.Cookie
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.get

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

                call.response.cookies.append(
                    Cookie(
                        name = "auth_token",
                        value = tokenResponse.accessToken,
                        secure = false, // false dla localhost, true dla HTTPS
                        httpOnly = true,
                        path = "/",
                        maxAge = 3600,
                        extensions = mapOf("SameSite" to SameSite.None)
                    )
                )

                // Zwróć tokeny w response body (nie w cookies!)
                call.respond(HttpStatusCode.OK, AuthResponse(
                    loginResponse = loginResponse,
                    tokenResponse = tokenResponse,
                ))
            } catch (e: Exception) {
                println("Exception in /auth/login")
                call.respond(HttpStatusCode.ExpectationFailed, "Login failed")
            }
        }
//        authenticate("refresh-jwt") {
            post("/refresh") {
                try {
                    val token = call.receive<RefreshTokenRequest>()
                    println("token: $token")

                    val principal = authService.validateRefreshToken(token.refreshToken)
                    println("principal: $principal")

                    if (principal == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid refresh token")
                        return@post
                    }

                    if (principal.userId == null) {
                        call.respond(HttpStatusCode.Unauthorized, "User ID not found in token")
                        return@post
                    }

                    val tokenResponse = authService.generateTokenResponse(LoginResponse(principal.userId, "admin"))
                    println("tokenResponse: $tokenResponse")

                    call.response.cookies.append(
                        Cookie(
                            name = "auth_token",
                            value = tokenResponse.accessToken,
                            secure = false, // false dla localhost, true dla HTTPS
                            httpOnly = true,
                            path = "/",
                            maxAge = 3600,
                            extensions = mapOf("SameSite" to SameSite.None)
                        )
                    )
                    call.response.cookies.append(
                        Cookie(
                            name = TokenType.REFRESH.name,
                            value = tokenResponse.refreshToken!!,
                            secure = false, // false dla localhost, true dla HTTPS
                            httpOnly = true,
                            path = "/",
                            maxAge = 3600,
                            extensions = mapOf("SameSite" to SameSite.None)
                        )
                    )
                    call.respond(
                        HttpStatusCode.OK,
                        TokenResponse(
                            accessToken = tokenResponse.accessToken,
                            refreshToken = tokenResponse.refreshToken,
                            expiresIn = tokenResponse.expiresIn
                        )
                    )

                } catch (e: Exception) {
                    println("Exception in /auth/refresh")
                    call.respond(HttpStatusCode.Unauthorized, "Invalid refresh token")
                }
            }
//        }

        authenticate("auth-jwt") {
            post("/logout") {
                try {
//                    val refreshToken = call.request.cookies["auth_token"]
//                    val tokenResponse = authService.generateTokenResponse(loginResponse)
//
//                    call.response.cookies.append(
//                        Cookie(
//                            name = "auth_token",
//                            value = tokenResponse.accessToken,
//                            secure = false, // false dla localhost, true dla HTTPS
//                            httpOnly = true,
//                            path = "/",
//                            maxAge = 3600,
//                            extensions = mapOf("SameSite" to SameSite.None)
//                        )
//                    )
//
//                    // Zwróć tokeny w response body (nie w cookies!)
//                    call.respond(HttpStatusCode.OK, AuthResponse(
//                        loginResponse = loginResponse,
//                        tokenResponse = tokenResponse,
//                    ))


                } catch (e: Exception) {
                    println("Exception in /auth/logout")
                }
            }
        }
    }
}