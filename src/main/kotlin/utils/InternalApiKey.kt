package com.kontenery.utils

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header

fun ApplicationCall.isValidInternalApiKey(): Boolean {
    val expected = System.getenv("INTERNAL_API_KEY")
    if (expected.isNullOrBlank()) return false
    return request.header("X-Internal-Key") == expected
}
