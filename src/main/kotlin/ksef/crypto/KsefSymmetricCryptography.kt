package com.kontenery.ksef.crypto

import com.kontenery.ksef.dto.KsefEncryptionInfo
import com.kontenery.ksef.dto.KsefPublicKeyCertificate
import com.kontenery.ksef.exception.KsefException
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.spec.MGF1ParameterSpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.crypto.spec.SecretKeySpec

data class KsefEncryptionData(
    val cipherKey: ByteArray,
    val cipherIv: ByteArray,
    val encryptionInfo: KsefEncryptionInfo,
)

data class KsefFileMetadata(
    val fileSize: Long,
    val hashSha256Base64: String,
)

object KsefSymmetricCryptography {

    private const val BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----"
    private const val END_CERTIFICATE = "-----END CERTIFICATE-----"

    fun resolveSymmetricKeyCertificate(certificates: List<KsefPublicKeyCertificate>): KsefPublicKeyCertificate {
        val now = Clock.System.now()
        return certificates
            .filter { cert -> cert.usage.any { it.equals("SymmetricKeyEncryption", ignoreCase = true) } }
            .filter { cert ->
                val validFrom = cert.validFrom?.let { Instant.parse(it) }
                val validTo = cert.validTo?.let { Instant.parse(it) }
                (validFrom == null || validFrom <= now) && (validTo == null || now < validTo)
            }
            .maxByOrNull { it.validFrom?.let { Instant.parse(it) } ?: Instant.DISTANT_PAST }
            ?: throw KsefException("No valid KSeF symmetric key encryption certificate found")
    }

    fun createEncryptionData(certificate: KsefPublicKeyCertificate): KsefEncryptionData {
        val key = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val publicKey = parsePublicKey(certificate.certificate)
        val encryptedKey = encryptRsaOaepSha256(key, publicKey)
        val encryptionInfo = KsefEncryptionInfo(
            encryptedSymmetricKey = Base64.getEncoder().encodeToString(encryptedKey),
            initializationVector = Base64.getEncoder().encodeToString(iv),
            publicKeyId = certificate.publicKeyId,
        )
        return KsefEncryptionData(key, iv, encryptionInfo)
    }

    fun encryptAes256Cbc(content: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
            return cipher.doFinal(content)
        } catch (e: Exception) {
            throw KsefException("Failed to encrypt invoice content for KSeF", cause = e)
        }
    }

    fun metadata(content: ByteArray): KsefFileMetadata {
        val digest = MessageDigest.getInstance("SHA-256").digest(content)
        return KsefFileMetadata(
            fileSize = content.size.toLong(),
            hashSha256Base64 = Base64.getEncoder().encodeToString(digest),
        )
    }

    private fun encryptRsaOaepSha256(content: ByteArray, publicKey: java.security.PublicKey): ByteArray {
        try {
            val oaepParams = OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT)
            val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams)
            return cipher.doFinal(content)
        } catch (e: Exception) {
            throw KsefException("Failed to encrypt symmetric key for KSeF", cause = e)
        }
    }

    private fun parsePublicKey(certificateBase64: String): java.security.PublicKey {
        val pem = buildString {
            appendLine(BEGIN_CERTIFICATE)
            appendLine(certificateBase64.trim())
            appendLine(END_CERTIFICATE)
        }
        val factory = CertificateFactory.getInstance("X.509")
        return factory.generateCertificate(pem.byteInputStream(StandardCharsets.UTF_8)).publicKey
    }
}
