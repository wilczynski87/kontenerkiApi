package com.kontenery.ksef.dto

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KsefStatusInfoDecodingTest {

    @Test
    fun `decodes optional details field`() {
        val json = Json { ignoreUnknownKeys = true }
        val decoded = json.decodeFromString<KsefStatusInfo>(
            """{"code":450,"description":"Błąd weryfikacji semantyki dokumentu faktury","details":"P_12 invalid value"}""",
        )
        assertEquals(450, decoded.code)
        assertEquals("P_12 invalid value", decoded.details)
    }
}

