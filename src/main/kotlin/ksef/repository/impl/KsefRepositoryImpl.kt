package com.kontenery.ksef.repository.impl

import com.kontenery.ksef.client.KsefApiClient
import com.kontenery.ksef.dto.KsefAuthKsefTokenRequest
import com.kontenery.ksef.dto.KsefAuthOperationStatusResponse
import com.kontenery.ksef.dto.KsefAuthStatusResponse
import com.kontenery.ksef.dto.KsefContextIdentifier
import com.kontenery.ksef.dto.KsefInvoiceQueryFilters
import com.kontenery.ksef.dto.KsefPublicKeyCertificate
import com.kontenery.ksef.dto.KsefQueryInvoiceMetadataResponse
import com.kontenery.ksef.dto.KsefSignatureResponse
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.repository.KsefRepository
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
}
