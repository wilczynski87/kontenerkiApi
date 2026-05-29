package com.kontenery.ksef.exception

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class KsefErrorMessagesTest {

    @Test
    fun `userMessage exposes KSeF processing code and description`() {
        val message = KsefErrorMessages.userMessage(
            KsefException("KSeF invoice processing failed: 450 Invalid FA structure"),
        )
        assertEquals("KSeF invoice processing failed: 450 Invalid FA structure", message)
    }

    @Test
    fun `userMessage strips invoice payload from legacy errors`() {
        val message = KsefErrorMessages.userMessage(
            KsefException("Problem to sendInvoiceToKsef:\n error: timeout invoice:{seller=...}"),
        )
        assertFalse(message.contains("seller="))
        assertEquals("KSeF invoice send failed: error: timeout", message)
    }

    @Test
    fun `userMessage maps not found status`() {
        val message = KsefErrorMessages.userMessage(
            KsefException("Invoice not found: FV/1", statusCode = 404),
        )
        assertEquals("Invoice not found: FV/1", message)
    }

    @Test
    fun `userMessage passes through mapper validation errors`() {
        val message = KsefErrorMessages.userMessage(
            KsefException("Unsupported VAT rate for KSeF P_12: 0.23"),
        )
        assertEquals("Unsupported VAT rate for KSeF P_12: 0.23", message)
    }

    @Test
    fun `userMessage passes through token configuration hints`() {
        val message = KsefErrorMessages.userMessage(
            KsefException("KSEF_TOKEN is the placeholder from .env.example"),
        )
        assertEquals("KSEF_TOKEN is the placeholder from .env.example", message)
    }
}
