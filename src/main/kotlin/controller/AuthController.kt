package com.kontenery.controller

import com.kontenery.library.model.auth.*
import com.kontenery.service.AuthService
import com.kontenery.service.RefreshTokenRequest
import com.kontenery.service.TokenType
import com.kontenery.service.ChangePasswordResult
import com.kontenery.utils.ApiErrorResponse
import com.kontenery.utils.respondBadRequest
import com.kontenery.utils.respondInternalError
import com.kontenery.utils.respondUnauthorized
import io.ktor.http.HttpStatusCode
import io.ktor.http.Cookie
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sessions.SameSite
import org.slf4j.LoggerFactory

private val authLog = LoggerFactory.getLogger("AuthController")

fun Route.authController(
    authService: AuthService
) {
    route("/auth") {
        post("/login") {
            try {
                val credentials: LoginRequest = call.receive<LoginRequest>()

                val loginResponse: LoginResponse = authService.login(credentials) ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiErrorResponse("Login failed")
                )

                val tokenResponse = authService.generateTokenResponse(loginResponse)

                call.response.cookies.append(
                    Cookie(
                        name = "auth_token",
                        value = tokenResponse.accessToken,
                        secure = false,
                        httpOnly = true,
                        path = "/",
                        maxAge = 3600,
                        extensions = mapOf("SameSite" to SameSite.None)
                    )
                )

                call.respond(HttpStatusCode.OK, AuthResponse(
                    loginResponse = loginResponse,
                    tokenResponse = tokenResponse,
                ))
            } catch (e: Exception) {
                call.respondInternalError(e, "Login failed")
            }
        }

        post("/refresh") {
            try {
                val token = call.receive<RefreshTokenRequest>()

                val principal = authService.validateRefreshToken(token.refreshToken)

                if (principal == null) {
                    call.respondUnauthorized("Invalid refresh token")
                    return@post
                }

                if (principal.userId == null) {
                    call.respondUnauthorized("User ID not found in token")
                    return@post
                }

                val tokenResponse = authService.generateTokenResponse(
                    LoginResponse(principal.userId, principal.role ?: "customer")
                )

                call.response.cookies.append(
                    Cookie(
                        name = "auth_token",
                        value = tokenResponse.accessToken,
                        secure = false,
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
                        secure = false,
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
                call.respondUnauthorized("Invalid refresh token", e)
            }
        }

        authenticate("auth-jwt") {

            get("/verify") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                    val tokenResponse = authService.generateTokenResponse(
                        LoginResponse(
                            principal?.payload?.getClaim("userId")?.asString().orEmpty(),
                            principal?.payload?.getClaim("role")?.asString() ?: "customer"
                        )
                    )

                    val authResponse = AuthResponse(
                        loginResponse = LoginResponse(
                            principal?.payload?.getClaim("userId")?.asString().orEmpty(),
                            principal?.payload?.getClaim("role")?.asString() ?: "customer"
                        ),
                        tokenResponse = tokenResponse
                    )

                    call.response.cookies.append(
                        Cookie(
                            name = "auth_token",
                            value = tokenResponse.accessToken,
                            secure = false,
                            httpOnly = true,
                            path = "/",
                            maxAge = 3600,
                            extensions = mapOf("SameSite" to SameSite.None)
                        )
                    )

                    call.respond(HttpStatusCode.OK, authResponse)

                } catch (e: Exception) {
                    authLog.warn("Token verification failed", e)
                    call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("Invalid token"))
                }
            }

            post("/logout") {
                try {
                    call.respond(HttpStatusCode.OK, ApiErrorResponse("Logged out"))
                } catch (e: Exception) {
                    authLog.warn("Logout failed", e)
                    call.respond(HttpStatusCode.OK, ApiErrorResponse("Logged out"))
                }
            }

            post("/change-password") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@post call.respondUnauthorized("Unauthorized")
                    val userId = principal.payload.getClaim("userId").asString()
                        ?: return@post call.respondUnauthorized("User ID not found in token")
                    val body = call.receive<ChangePasswordRequest>()

                    when (val result = authService.changePassword(userId, body)) {
                        is ChangePasswordResult.Ok ->
                            call.respond(HttpStatusCode.OK, ApiErrorResponse("Password changed"))
                        is ChangePasswordResult.InvalidCurrent ->
                            call.respondUnauthorized("Invalid current password")
                        is ChangePasswordResult.BadRequest ->
                            call.respondBadRequest(result.message)
                        is ChangePasswordResult.Forbidden ->
                            call.respond(HttpStatusCode.Forbidden, ApiErrorResponse(result.message))
                        is ChangePasswordResult.NotFound ->
                            call.respond(HttpStatusCode.NotFound, ApiErrorResponse("Client not found"))
                    }
                } catch (e: Exception) {
                    call.respondInternalError(e, "Failed to change password")
                }
            }
        }
    }
}
