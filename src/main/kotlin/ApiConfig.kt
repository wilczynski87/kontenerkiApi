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
data class KsefConfig(
    val environment: String,
    val baseUrl: String,
    val apiSuffix: String,
    val token: String?,
    val nip: String?,
)

@Serializable
data class ApiConfig(
    val env: String,
    val email: EmailConfig,
    val db: DbConfig,
    val auth: AuthConfig,
    val ksef: KsefConfig,
)

private fun env(name: String, default: String): String =
    System.getenv(name)?.ifBlank { null } ?: default

private fun envRequired(name: String): String =
    System.getenv(name)?.ifBlank { null }
        ?: error("Required environment variable $name is not set")

private fun envOrNull(name: String): String? =
    System.getenv(name)?.ifBlank { null }

/** NIP sprzedawcy (Kontenery) — domyślny kontekst KSeF w DEV, gdy KSEF_NIP nie jest ustawiony. */
internal const val KSEF_DEV_DEFAULT_NIP = "8943278612"

internal fun resolveKsefConfig(
    apiEnv: String,
    getenv: (String) -> String? = { System.getenv(it) },
): KsefConfig {
    val isDev = apiEnv.equals("DEV", ignoreCase = true)
    val ksefEnvironment = if (isDev) KsefEnvironment.DEV_DEFAULT else KsefEnvironment.PRODUCTION

    val baseUrl = getenv("KSEF_BASE_URL")?.trim()?.takeIf { it.isNotBlank() }
        ?: ksefEnvironment.baseUrl
    val apiSuffix = getenv("KSEF_API_SUFFIX")?.trim()?.takeIf { it.isNotBlank() }
        ?: ksefEnvironment.apiSuffix

    if (isDev && baseUrl.contains("api.ksef.mf.gov.pl") && !baseUrl.contains("-test") && !baseUrl.contains("-demo")) {
        error(
            "API_ENV=DEV nie może używać produkcyjnego KSEF_BASE_URL ($baseUrl). " +
                "Ustaw KSEF_ENV=TEST lub KSEF_BASE_URL=https://api-test.ksef.mf.gov.pl",
        )
    }

    val nip = getenv("KSEF_NIP")?.trim()?.takeIf { it.isNotEmpty() }
        ?: if (isDev) KSEF_DEV_DEFAULT_NIP else null

    val tokenFromFile = getenv("KSEF_TOKEN_FILE")?.trim()?.takeIf { it.isNotEmpty() }?.let { path ->
        runCatching { java.io.File(path).readText().trim() }
            .getOrElse { throw IllegalStateException("Cannot read KSEF_TOKEN_FILE at $path: ${it.message}") }
    }
    val token = getenv("KSEF_TOKEN")?.trim()?.takeIf { it.isNotEmpty() } ?: tokenFromFile

    return KsefConfig(
        environment = ksefEnvironment.name,
        baseUrl = baseUrl,
        apiSuffix = apiSuffix,
        token = token,
        nip = nip,
    )
}

fun Application.loadApiConfig(): ApiConfig {
    val apiEnv = env("API_ENV", "DEV")
    return ApiConfig(
        env = apiEnv,

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
            refreshTokenExpiry = env("VALIDITY_REFRESH_MS", "2592000000").toLong(),
            googleClientId = env("GOOGLE_CLIENT_ID", "1234567890"),
            appLogin = envOrNull("APP_LOGIN"),
            appSecret = envOrNull("APP_SECRET"),
        ),

        ksef = resolveKsefConfig(apiEnv),
    )
}