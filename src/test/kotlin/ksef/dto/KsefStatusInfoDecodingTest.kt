package com.kontenery.ksef.dto

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class KsefStatusInfoDecodingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes details as string`() {
        val decoded = json.decodeFromString<KsefStatusInfo>(
            """{"code":450,"description":"Błąd weryfikacji semantyki dokumentu faktury","details":"P_12 invalid value"}""",
        )
        assertEquals(450, decoded.code)
        assertEquals("P_12 invalid value", decoded.details)
    }

    @Test
    fun `decodes details as json array of strings`() {
        val decoded = json.decodeFromString<KsefStatusInfo>(
            """
            {
              "code": 450,
              "description": "Błąd weryfikacji semantyki dokumentu faktury",
              "details": [
                "The 'http://crd.gov.pl/wzor/2025/06/25/13775/:Fa' element is invalid.",
                "P_12: invalid value"
              ]
            }
            """.trimIndent(),
        )
        assertEquals(450, decoded.code)
        assertTrue(decoded.details!!.contains("element is invalid"))
        assertTrue(decoded.details!!.contains("P_12"))
        assertTrue(decoded.details!!.contains("; "))
    }
}
