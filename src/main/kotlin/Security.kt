package com.kontenery

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.http.auth.AuthScheme
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.sql.Connection
import java.sql.DriverManager
import org.jetbrains.exposed.sql.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import java.security.MessageDigest
import kotlin.text.Charsets.UTF_8

fun Application.configureSecurity(apiConfig: ApiConfig) {
    // Please read the jwt property from the config file if you are using EngineMain
    val jwtAudience = apiConfig.auth.audience
//    val jwtDomain = apiConfig.auth.
    val jwtIssuer = apiConfig.auth.issuer
    val jwtRealm = apiConfig.auth.realm
    val jwtSecret = apiConfig.auth.secret
    val validityMs = 36_000_00 * 24 // 24h


    install(CORS) {
        allowCredentials = true                         // <- bardzo ważne
        allowHost("localhost:8080", schemes = listOf("http")) // frontend
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowNonSimpleContentTypes = true
    }

    install(Authentication) {
        jwt("auth-jwt") {
            realm = apiConfig.auth.realm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(apiConfig.auth.secret))
                    .withAudience(apiConfig.auth.audience)
                    .withIssuer(apiConfig.auth.issuer)
                    .build()
            )

            authHeader { call ->
                // czyta token z headera Authorization: Bearer
                val auth = call.request.headers[HttpHeaders.Authorization]
                if (auth != null && auth.startsWith("Bearer ")) {
                    HttpAuthHeader.Single(AuthScheme.Bearer, auth.removePrefix("Bearer ").trim())
                } else null
            }

            validate { credential ->
                val username = credential.payload.getClaim("role").asString()
                if (!username.isNullOrBlank()) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
