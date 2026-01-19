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
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val validityMs: Long,
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
            secret = cfg.propertyOrNull("api.auth.secret")?.getString() ?: "secret",
            issuer = cfg.propertyOrNull("api.auth.issuer")?.getString() ?: "ktor sample app",
            audience = cfg.propertyOrNull("api.auth.audience")?.getString() ?: "jwt-audience",
            realm = cfg.propertyOrNull("api.auth.realm")?.getString() ?: "ktor sample app",
            validityMs = cfg.propertyOrNull("api.auth.validity")?.getString()?.toLong() ?: 60000,
            googleClientId = cfg.propertyOrNull("api.auth.googleClientId")?.getString() ?: "1234567890"
        )
    )
}