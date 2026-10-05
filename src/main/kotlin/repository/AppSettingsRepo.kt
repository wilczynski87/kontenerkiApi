package com.kontenery.repository

interface AppSettingsRepo {
    suspend fun get(key: String): String?
    suspend fun set(key: String, value: String)
}
