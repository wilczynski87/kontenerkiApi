package com.kontenery

import com.kontenery.service.JwtConfig
import io.ktor.http.*
import io.ktor.http.auth.AuthScheme
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureSecurity(jwtConfig: JwtConfig) {

    install(CORS) {
        allowCredentials = true
        allowNonSimpleContentTypes = true
        allowSameOrigin = true

        anyHost()

        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-No-Auth")

        allowNonSimpleContentTypes = true

        exposeHeader(HttpHeaders.Authorization)
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(jwtConfig.accessTokenVerifier)

            authHeader { call ->
                // czyta token z headera Authorization: Bearer
                val auth = call.request.headers[HttpHeaders.Authorization]
                if (auth != null && auth.startsWith("Bearer ")) {
                    HttpAuthHeader.Single(AuthScheme.Bearer, auth.removePrefix("Bearer ").trim())
                } else null
            }

            validate { credential ->
                val username = credential.payload.getClaim("userId").asString()
                if (!username.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
        jwt("refresh-jwt") {
            verifier(jwtConfig.refreshTokenVerifier)

            validate { credential ->
                println("refresh-jwt - Validator")
                val username = credential.payload.getClaim("userId").asString()
                if (!username.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
