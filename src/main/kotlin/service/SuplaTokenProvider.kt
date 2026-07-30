package com.kontenery.service

/**
 * Dostarcza ważny access token SUPLA.
 * Odświeża automatycznie przez refresh_token i persystuje rotację w PostgreSQL.
 */
interface SuplaTokenProvider {
    /** Zwraca ważny access token; w razie potrzeby odświeża. */
    suspend fun getAccessToken(forceRefresh: Boolean = false): String

    /** Proaktywnie odświeża token, jeśli wkrótce wygaśnie (background job). */
    suspend fun ensureFreshToken()

    /** Zapisuje tokeny uzyskane z OAuth exchange-code. */
    suspend fun storeTokens(
        accessToken: String,
        refreshToken: String?,
        expiresInSeconds: Long,
    )
}
