package com.kontenery.repository

data class SuplaStoredTokens(
    val accessToken: String?,
    val refreshToken: String?,
    val accessTokenExpiresAtEpochMs: Long?,
)

interface SuplaTokenRepo {
    suspend fun load(): SuplaStoredTokens?
    suspend fun save(tokens: SuplaStoredTokens)
}
