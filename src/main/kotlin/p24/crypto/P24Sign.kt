package com.kontenery.p24.crypto

import java.security.MessageDigest
import java.util.HexFormat

/**
 * Przelewy24 REST API v1: sign = SHA-384(compact JSON with fixed field order).
 * Field sets differ per operation — never reorder keys.
 */
object P24Sign {
    fun sha384Hex(payload: String): String {
        val digest = MessageDigest.getInstance("SHA-384").digest(payload.toByteArray(Charsets.UTF_8))
        return HexFormat.of().formatHex(digest)
    }

    fun registerSign(
        sessionId: String,
        merchantId: Int,
        amount: Int,
        currency: String,
        crc: String,
    ): String {
        val payload =
            """{"sessionId":${jsonString(sessionId)},"merchantId":$merchantId,"amount":$amount,"currency":${jsonString(currency)},"crc":${jsonString(crc)}}"""
        return sha384Hex(payload)
    }

    fun verifySign(
        sessionId: String,
        orderId: Long,
        amount: Int,
        currency: String,
        crc: String,
    ): String {
        val payload =
            """{"sessionId":${jsonString(sessionId)},"orderId":$orderId,"amount":$amount,"currency":${jsonString(currency)},"crc":${jsonString(crc)}}"""
        return sha384Hex(payload)
    }

    fun notificationSign(
        merchantId: Int,
        posId: Int,
        sessionId: String,
        amount: Int,
        originAmount: Int,
        currency: String,
        orderId: Long,
        methodId: Int,
        statement: String,
        crc: String,
    ): String {
        val payload =
            """{"merchantId":$merchantId,"posId":$posId,"sessionId":${jsonString(sessionId)},"amount":$amount,"originAmount":$originAmount,"currency":${jsonString(currency)},"orderId":$orderId,"methodId":$methodId,"statement":${jsonString(statement)},"crc":${jsonString(crc)}}"""
        return sha384Hex(payload)
    }

    fun isValidNotificationSign(
        merchantId: Int,
        posId: Int,
        sessionId: String,
        amount: Int,
        originAmount: Int,
        currency: String,
        orderId: Long,
        methodId: Int,
        statement: String,
        crc: String,
        providedSign: String,
    ): Boolean {
        val expected = notificationSign(
            merchantId = merchantId,
            posId = posId,
            sessionId = sessionId,
            amount = amount,
            originAmount = originAmount,
            currency = currency,
            orderId = orderId,
            methodId = methodId,
            statement = statement,
            crc = crc,
        )
        return expected.equals(providedSign, ignoreCase = true)
    }

    private fun jsonString(value: String): String =
        buildString {
            append('"')
            value.forEach { ch ->
                when (ch) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(ch)
                }
            }
            append('"')
        }
}
