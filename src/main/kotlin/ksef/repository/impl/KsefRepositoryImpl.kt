package com.kontenery.ksef.repository.impl

import com.kontenery.ksef.client.KsefApiClient
import com.kontenery.ksef.dto.KsefAuthKsefTokenRequest
import com.kontenery.ksef.dto.KsefAuthOperationStatusResponse
import com.kontenery.ksef.dto.KsefAuthStatusResponse
import com.kontenery.ksef.dto.KsefContextIdentifier
import com.kontenery.ksef.crypto.KsefEncryptionData
import com.kontenery.ksef.crypto.KsefSymmetricCryptography
import com.kontenery.ksef.dto.KsefFormCode
import com.kontenery.ksef.dto.KsefInvoiceQueryFilters
import com.kontenery.ksef.dto.KsefOpenOnlineSessionRequest
import com.kontenery.ksef.dto.KsefOpenOnlineSessionResponse
import com.kontenery.ksef.dto.KsefPublicKeyCertificate
import com.kontenery.ksef.dto.KsefQueryInvoiceMetadataResponse
import com.kontenery.ksef.dto.KsefSendInvoiceOnlineRequest
import com.kontenery.ksef.dto.KsefSendInvoiceOnlineResponse
import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse
import com.kontenery.ksef.dto.KsefSessionStatusResponse
import com.kontenery.ksef.dto.KsefSignatureResponse
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.repository.KsefRepository
import java.util.Base64
import kotlinx.datetime.Instant

class KsefRepositoryImpl(
    private val apiClient: KsefApiClient,
) : KsefRepository {

    override suspend fun fetchPublicKeyCertificates(): List<KsefPublicKeyCertificate> =
        apiClient.getPublicKeyCertificates()

    override suspend fun fetchAuthChallenge(): Pair<String, Long> {
        val challenge = apiClient.getAuthChallenge()
        val timestampMs = challenge.timestampMs
            ?: challenge.timestamp?.let { Instant.parse(it).toEpochMilliseconds() }
            ?: throw KsefException("KSeF auth challenge response missing timestamp")
        return challenge.challenge to timestampMs
    }

    override suspend fun submitKsefTokenAuth(
        challenge: String,
        nip: String,
        encryptedToken: String,
        publicKeyId: String?,
    ): KsefSignatureResponse {
        val request = KsefAuthKsefTokenRequest(
            challenge = challenge,
            contextIdentifier = KsefContextIdentifier(type = "Nip", value = nip),
            encryptedToken = encryptedToken,
            publicKeyId = publicKeyId,
        )
        return apiClient.authenticateByKsefToken(request)
    }

    override suspend fun fetchAuthStatus(
        referenceNumber: String,
        authenticationToken: String,
    ): KsefAuthStatusResponse =
        apiClient.getAuthStatus(referenceNumber, authenticationToken)

    override suspend fun redeemAuthToken(authenticationToken: String): KsefAuthOperationStatusResponse =
        apiClient.redeemToken(authenticationToken)

    override suspend fun queryInvoices(
        accessToken: String,
        pageOffset: Int,
        pageSize: Int,
        sortOrder: String,
        filters: KsefInvoiceQueryFilters,
    ): KsefQueryInvoiceMetadataResponse =
        apiClient.queryInvoiceMetadata(accessToken, pageOffset, pageSize, sortOrder, filters)

    override suspend fun createEncryptionData(): KsefEncryptionData {
        val certificate = KsefSymmetricCryptography.resolveSymmetricKeyCertificate(fetchPublicKeyCertificates())
        return KsefSymmetricCryptography.createEncryptionData(certificate)
    }

    override suspend fun openOnlineSession(
        accessToken: String,
        encryptionData: KsefEncryptionData,
    ): KsefOpenOnlineSessionResponse {
        val request = KsefOpenOnlineSessionRequest(
            formCode = KsefFormCode(
                systemCode = "FA (3)",
                schemaVersion = "1-0E",
                value = "FA",
            ),
            encryption = encryptionData.encryptionInfo,
        )
        return apiClient.openOnlineSession(request, accessToken)
    }

    override suspend fun sendInvoiceToSession(
        accessToken: String,
        sessionReferenceNumber: String,
        invoiceXml: ByteArray,
        encryptionData: KsefEncryptionData,
    ): KsefSendInvoiceOnlineResponse {
        val encrypted = KsefSymmetricCryptography.encryptAes256Cbc(
            invoiceXml,
            encryptionData.cipherKey,
            encryptionData.cipherIv,
        )
        val invoiceMetadata = KsefSymmetricCryptography.metadata(invoiceXml)
        val encryptedMetadata = KsefSymmetricCryptography.metadata(encrypted)
        val request = KsefSendInvoiceOnlineRequest(
            invoiceHash = invoiceMetadata.hashSha256Base64,
            invoiceSize = invoiceMetadata.fileSize,
            encryptedInvoiceHash = encryptedMetadata.hashSha256Base64,
            encryptedInvoiceSize = encryptedMetadata.fileSize,
            encryptedInvoiceContent = Base64.getEncoder().encodeToString(encrypted),
        )
        return apiClient.sendInvoiceToOnlineSession(sessionReferenceNumber, request, accessToken)
    }

    override suspend fun closeOnlineSession(accessToken: String, sessionReferenceNumber: String) {
        apiClient.closeOnlineSession(sessionReferenceNumber, accessToken)
    }

    override suspend fun fetchSessionStatus(
        accessToken: String,
        sessionReferenceNumber: String,
    ): KsefSessionStatusResponse =
        apiClient.getSessionStatus(sessionReferenceNumber, accessToken)

    override suspend fun fetchSessionInvoiceStatus(
        accessToken: String,
        sessionReferenceNumber: String,
        invoiceReferenceNumber: String,
    ): KsefSessionInvoiceStatusResponse =
        apiClient.getSessionInvoiceStatus(sessionReferenceNumber, invoiceReferenceNumber, accessToken)

    override suspend fun downloadInvoiceByKsefNumber(accessToken: String, ksefNumber: String): ByteArray =
        apiClient.downloadInvoiceByKsefNumber(ksefNumber, accessToken)
}
