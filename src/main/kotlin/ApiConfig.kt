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
    val appLogin: String?,
    val appSecret: String?,
)
@Serializable
data class ApiConfig(
    val env: String,
    val email: EmailConfig,
    val db: DbConfig,
    val auth: AuthConfig,
)

private fun env(name: String, default: String): String =
    System.getenv(name)?.ifBlank { null } ?: default

private fun envRequired(name: String): String =
    System.getenv(name)?.ifBlank { null }
        ?: error("Required environment variable $name is not set")

private fun envOrNull(name: String): String? =
    System.getenv(name)?.ifBlank { null }

fun Application.loadApiConfig(): ApiConfig {
    return ApiConfig(
        env = env("API_ENV", "DEV"),

        email = EmailConfig(
            host = env("EMAIL_HOST", "localhost"),
            port = env("EMAIL_PORT", "8200").toInt()
        ),

        db = DbConfig(
            host = env("DB_HOST", "localhost"),
            port = env("DB_PORT", "5431").toInt(),
            name = env("DB_NAME", "db1"),
            user = env("DB_USER", "admin_user"),
            password = env("DB_PASSWORD", "postgres")
        ),

        auth = AuthConfig(
            secretAuth = envRequired("JWT_SECRET"),
            secretRefresh = envRequired("JWT_SECRET"),
            issuer = env("JWT_ISSUER", "ktor sample app"),
            audience = env("JWT_AUDIENCE", "jwt-audience"),
            realm = env("JWT_REALM", "ktor sample app"),
            accessTokenExpiry = env("VALIDITY_MS", "3600000").toLong(),
            refreshTokenExpiry = env("VALIDITY_MS", "2592000000").toLong(),
            googleClientId = env("GOOGLE_CLIENT_ID", "1234567890"),
            appLogin = envOrNull("APP_LOGIN"),
            appSecret = envOrNull("APP_SECRET"),
        )
    )
}