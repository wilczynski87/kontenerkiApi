package com.kontenery

import io.ktor.server.application.Application
import kotlinx.serialization.Serializable

@Serializable
data class EmailConfig(
    val host: String,
    val port: Int
)
@Serializable
data class DbConfig(
    val host: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String
)
@Serializable
data class AuthConfig(
    val secretAuth: String?,
    val secretRefresh: String?,
    val issuer: String,
    val audience: String,
    val realm: String,
    val accessTokenExpiry: Long = 3600000,
    val refreshTokenExpiry: Long = 2592000000,
    val googleClientId: String,
)
@Serializable
data class ApiConfig(
    val env: String,
    val email: EmailConfig,
    val db: DbConfig,
    val auth: AuthConfig,
)

fun Application.loadApiConfig(): ApiConfig {
    val cfg = environment.config

    return ApiConfig(
        env = cfg.propertyOrNull("api.env")?.getString() ?: "DEV",

        email = EmailConfig(
            host = cfg.propertyOrNull("api.email.host")?.getString() ?: "localhost",
            port = cfg.propertyOrNull("api.email.port")?.getString()?.toInt() ?: 8200
        ),

        db = DbConfig(
            host = cfg.propertyOrNull("api.db.host")?.getString() ?: "localhost",
            port = cfg.propertyOrNull("api.db.port")?.getString()?.toInt() ?: 5431,
            name = cfg.propertyOrNull("api.db.name")?.getString() ?: "db1",
            user = cfg.propertyOrNull("api.db.user")?.getString() ?: "admin_user",
            password = cfg.propertyOrNull("api.db.password")?.getString() ?: "postgres"
        ),
        auth = AuthConfig(
            secretAuth = cfg.propertyOrNull("api.auth.secretAuth")?.getString() ?: "secretAuth",
            secretRefresh = cfg.propertyOrNull("api.auth.secretRefresh")?.getString() ?: "secretRefresh",
            issuer = cfg.propertyOrNull("api.auth.issuer")?.getString() ?: "ktor sample app",
            audience = cfg.propertyOrNull("api.auth.audience")?.getString() ?: "jwt-audience",
            realm = cfg.propertyOrNull("api.auth.realm")?.getString() ?: "ktor sample app",
            accessTokenExpiry = cfg.propertyOrNull("api.auth.validity")?.getString()?.toLong() ?: 3600000,
            refreshTokenExpiry = cfg.propertyOrNull("api.auth.validity")?.getString()?.toLong() ?: 2592000000,
            googleClientId = cfg.propertyOrNull("api.auth.googleClientId")?.getString() ?: "1234567890"
        )
    )
}