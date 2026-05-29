package com.kontenery.ksef.repository

import com.kontenery.ksef.dto.KsefAuthOperationStatusResponse
import com.kontenery.ksef.dto.KsefAuthStatusResponse
import com.kontenery.ksef.dto.KsefInvoiceQueryFilters
import com.kontenery.ksef.dto.KsefPublicKeyCertificate
import com.kontenery.ksef.dto.KsefOpenOnlineSessionResponse
import com.kontenery.ksef.dto.KsefQueryInvoiceMetadataResponse
import com.kontenery.ksef.dto.KsefSendInvoiceOnlineResponse
import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse
import com.kontenery.ksef.dto.KsefSessionStatusResponse
import com.kontenery.ksef.dto.KsefSignatureResponse
import com.kontenery.ksef.crypto.KsefEncryptionData

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
    suspend fun createEncryptionData(): KsefEncryptionData
    suspend fun openOnlineSession(accessToken: String, encryptionData: KsefEncryptionData): KsefOpenOnlineSessionResponse
    suspend fun sendInvoiceToSession(
        accessToken: String,
        sessionReferenceNumber: String,
        invoiceXml: ByteArray,
        encryptionData: KsefEncryptionData,
    ): KsefSendInvoiceOnlineResponse
    suspend fun closeOnlineSession(accessToken: String, sessionReferenceNumber: String)
    suspend fun fetchSessionStatus(accessToken: String, sessionReferenceNumber: String): KsefSessionStatusResponse
    suspend fun fetchSessionInvoiceStatus(
        accessToken: String,
        sessionReferenceNumber: String,
        invoiceReferenceNumber: String,
    ): KsefSessionInvoiceStatusResponse
    suspend fun downloadInvoiceByKsefNumber(accessToken: String, ksefNumber: String): ByteArray
}
