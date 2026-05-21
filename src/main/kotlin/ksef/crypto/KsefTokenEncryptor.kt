package com.kontenery.ksef.crypto

import com.kontenery.ksef.exception.KsefException
import java.nio.charset.StandardCharsets
import java.security.cert.CertificateFactory
import java.security.spec.MGF1ParameterSpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

object KsefTokenEncryptor {

    private const val BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----"
    private const val END_CERTIFICATE = "-----END CERTIFICATE-----"

    fun encryptToken(ksefToken: String, timestampMs: Long, certificateBase64: String): String {
        val tokenWithTimestamp = "$ksefToken|$timestampMs"
        val encrypted = encryptWithRsaOaepSha256(
            tokenWithTimestamp.toByteArray(StandardCharsets.UTF_8),
            certificateBase64,
        )
        return Base64.getEncoder().encodeToString(encrypted)
    }

    private fun encryptWithRsaOaepSha256(content: ByteArray, certificateBase64: String): ByteArray {
        try {
            val publicKey = parsePublicKeyFromCertificate(certificateBase64)
            val oaepParams = OAEPParameterSpec(
                "SHA-256",
                "MGF1",
                MGF1ParameterSpec.SHA256,
                PSource.PSpecified.DEFAULT,
            )
            val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams)
            return cipher.doFinal(content)
        } catch (e: Exception) {
            throw KsefException("Failed to encrypt KSeF token", cause = e)
        }
    }

    private fun parsePublicKeyFromCertificate(certificateBase64: String): java.security.PublicKey {
        val pem = buildString {
            appendLine(BEGIN_CERTIFICATE)
            appendLine(certificateBase64.trim())
            appendLine(END_CERTIFICATE)
        }
        val factory = CertificateFactory.getInstance("X.509")
        return factory.generateCertificate(pem.byteInputStream(StandardCharsets.UTF_8)).publicKey
    }
}
