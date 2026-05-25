package com.kontenery.ksef

import com.kontenery.ksef.exception.KsefException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class KsefTokenDiagnosticsTest {

    private val sampleToken =
        "20260521-EC-1DA0238000-CA0B9654EA-AA|nip-8943278612|4bbbb1a9a28c4cd493077bf1057d3b34b87f8ba8ba6d4bec9e43d840110c0f74"

    @Test
    fun `nipFromToken parses NIP segment`() {
        assertEquals("8943278612", KsefTokenDiagnostics.nipFromToken(sampleToken))
    }

    @Test
    fun `validate rejects placeholder example token`() {
        val ex = assertThrows(KsefException::class.java) {
            KsefTokenDiagnostics.validateForAuthentication(sampleToken, "8943278612")
        }
        assertEquals(true, ex.message?.contains("placeholder") == true)
    }

    @Test
    fun `validate rejects nip mismatch`() {
        val ex = assertThrows(KsefException::class.java) {
            KsefTokenDiagnostics.validateForAuthentication(
                "prefix|nip-1111111111|othersecretpart",
                "8943278612",
            )
        }
        assertEquals(true, ex.message?.contains("8943278612") == true)
    }
}
