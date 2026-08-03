package com.kontenery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class P24ConfigResolutionTest {

    @Test
    fun `DEV defaults to sandbox mock when credentials missing`() {
        val config = resolveP24Config("DEV") { null }
        assertEquals("SANDBOX", config.environment)
        assertTrue(config.mockMode)
        assertTrue(config.baseUrl.contains("sandbox"))
    }

    @Test
    fun `explicit credentials disable mock`() {
        val env = mapOf(
            "P24_MERCHANT_ID" to "111",
            "P24_CRC" to "crc",
            "P24_API_KEY" to "key",
            "P24_MOCK" to "false",
        )
        val config = resolveP24Config("DEV") { env[it] }
        assertFalse(config.mockMode)
        assertEquals(111, config.merchantId)
        assertEquals(111, config.posId)
    }

    @Test
    fun `DEV rejects production environment`() {
        assertThrows(IllegalStateException::class.java) {
            resolveP24Config("DEV") { if (it == "P24_ENV") "PRODUCTION" else null }
        }
    }
}
