package com.kontenery

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GateConfigResolutionTest {

    @Test
    fun `DEV defaults to mock when GATE_OPEN_URL missing`() {
        val config = resolveGateConfig("DEV") { null }
        assertTrue(config.mockMode)
        assertEquals(60, config.cooldownSeconds)
        assertEquals("PATCH", config.method)
    }

    @Test
    fun `GATE_MOCK false disables mock even in DEV`() {
        val config = resolveGateConfig("DEV") { key ->
            when (key) {
                "GATE_MOCK" -> "false"
                "GATE_OPEN_URL" -> "https://example.com/gate"
                else -> null
            }
        }
        assertFalse(config.mockMode)
        assertEquals("https://example.com/gate", config.openUrl)
    }

    @Test
    fun `reads access token aliases and cooldown`() {
        val config = resolveGateConfig("PROD") { key ->
            when (key) {
                "GATE_OPEN_URL" -> "https://svr111.supla.org/api/v3/channels/1"
                "GATE_API_KEY" -> "token-abc"
                "GATE_COOLDOWN_SECONDS" -> "120"
                "GATE_MOCK" -> "false"
                else -> null
            }
        }
        assertEquals("token-abc", config.accessToken)
        assertEquals(120, config.cooldownSeconds)
        assertFalse(config.mockMode)
    }

    @Test
    fun `defaults request body to OPEN_CLOSE JSON object`() {
        val config = resolveGateConfig("PROD") { key ->
            when (key) {
                "GATE_OPEN_URL" -> "https://svr111.supla.org/api/v3/channels/1"
                "GATE_MOCK" -> "false"
                else -> null
            }
        }
        assertEquals("""{"action":"OPEN_CLOSE"}""", config.requestBody)
    }
}
