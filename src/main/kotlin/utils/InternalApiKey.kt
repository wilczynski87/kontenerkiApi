package com.kontenery.utils

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header

/**
 * Resolves the expected internal API key. Overridable in tests via [internalApiKeyProvider].
 */
var internalApiKeyProvider: () -> String? = { System.getenv("INTERNAL_API_KEY") }

fun ApplicationCall.isValidInternalApiKey(): Boolean {
    val expected = internalApiKeyProvider()
    if (expected.isNullOrBlank()) return false
    return request.header("X-Internal-Key") == expected
}
