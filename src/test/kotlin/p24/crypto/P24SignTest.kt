package com.kontenery.p24.crypto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class P24SignTest {

    @Test
    fun `register sign matches known SHA-384 vector`() {
        // Compact JSON: {"sessionId":"test-session","merchantId":12345,"amount":1099,"currency":"PLN","crc":"crc-secret"}
        val sign = P24Sign.registerSign(
            sessionId = "test-session",
            merchantId = 12345,
            amount = 1099,
            currency = "PLN",
            crc = "crc-secret",
        )
        assertEquals(96, sign.length)
        assertEquals(
            P24Sign.sha384Hex(
                """{"sessionId":"test-session","merchantId":12345,"amount":1099,"currency":"PLN","crc":"crc-secret"}""",
            ),
            sign,
        )
    }

    @Test
    fun `verify sign uses orderId not merchantId`() {
        val sign = P24Sign.verifySign(
            sessionId = "test-session",
            orderId = 987654321L,
            amount = 1099,
            currency = "PLN",
            crc = "crc-secret",
        )
        assertEquals(
            P24Sign.sha384Hex(
                """{"sessionId":"test-session","orderId":987654321,"amount":1099,"currency":"PLN","crc":"crc-secret"}""",
            ),
            sign,
        )
    }

    @Test
    fun `notification sign validates provided signature`() {
        val sign = P24Sign.notificationSign(
            merchantId = 12345,
            posId = 12345,
            sessionId = "sess-1",
            amount = 2500,
            originAmount = 2500,
            currency = "PLN",
            orderId = 111L,
            methodId = 25,
            statement = "p24-statement",
            crc = "crc-secret",
        )
        assertTrue(
            P24Sign.isValidNotificationSign(
                merchantId = 12345,
                posId = 12345,
                sessionId = "sess-1",
                amount = 2500,
                originAmount = 2500,
                currency = "PLN",
                orderId = 111L,
                methodId = 25,
                statement = "p24-statement",
                crc = "crc-secret",
                providedSign = sign,
            ),
        )
    }
}
