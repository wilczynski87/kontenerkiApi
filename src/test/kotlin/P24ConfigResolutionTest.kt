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
    fun `expands SERVER_URI in public payment urls`() {
        val env = mapOf(
            "SERVER_URI" to "57.131.144.33",
            "P24_URL_STATUS" to "http://\${SERVER_URI}:8100",
            "P24_PAYMENT_URL" to "http://\${SERVER_URI}:8100",
            "P24_MOCK" to "true",
        )
        val config = resolveP24Config("DEV") { env[it] }
        assertEquals("http://57.131.144.33:8100", config.urlStatus)
        assertEquals("http://57.131.144.33:8100", config.paymentUrl)
    }

    @Test
    fun `DEV rejects production environment`() {
        assertThrows(IllegalStateException::class.java) {
            resolveP24Config("DEV") { if (it == "P24_ENV") "PRODUCTION" else null }
        }
    }
}
