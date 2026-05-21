package com.kontenery.ksef.repository

import com.kontenery.ksef.dto.KsefAuthOperationStatusResponse
import com.kontenery.ksef.dto.KsefAuthStatusResponse
import com.kontenery.ksef.dto.KsefInvoiceQueryFilters
import com.kontenery.ksef.dto.KsefPublicKeyCertificate
import com.kontenery.ksef.dto.KsefQueryInvoiceMetadataResponse
import com.kontenery.ksef.dto.KsefSignatureResponse

interface KsefRepository {
    suspend fun fetchPublicKeyCertificates(): List<KsefPublicKeyCertificate>
    suspend fun fetchAuthChallenge(): Pair<String, Long>
    suspend fun submitKsefTokenAuth(
        challenge: String,
        nip: String,
        encryptedToken: String,
        publicKeyId: String?,
    ): KsefSignatureResponse
    suspend fun fetchAuthStatus(referenceNumber: String, authenticationToken: String): KsefAuthStatusResponse
    suspend fun redeemAuthToken(authenticationToken: String): KsefAuthOperationStatusResponse
    suspend fun queryInvoices(
        accessToken: String,
        pageOffset: Int,
        pageSize: Int,
        sortOrder: String,
        filters: KsefInvoiceQueryFilters,
    ): KsefQueryInvoiceMetadataResponse
}
