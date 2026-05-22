package com.kontenery.ksef.dto

import kotlinx.serialization.Serializable

@Serializable
data class KsefFormCode(
    val systemCode: String,
    val schemaVersion: String,
    val value: String,
)

@Serializable
data class KsefEncryptionInfo(
    val encryptedSymmetricKey: String,
    val initializationVector: String,
    val publicKeyId: String? = null,
)

@Serializable
data class KsefOpenOnlineSessionRequest(
    val formCode: KsefFormCode,
    val encryption: KsefEncryptionInfo,
)

@Serializable
data class KsefOpenOnlineSessionResponse(
    val referenceNumber: String,
    val validUntil: String? = null,
)

@Serializable
data class KsefSendInvoiceOnlineRequest(
    val invoiceHash: String,
    val invoiceSize: Long,
    val encryptedInvoiceHash: String,
    val encryptedInvoiceSize: Long,
    val encryptedInvoiceContent: String,
    val offlineMode: Boolean = false,
)

@Serializable
data class KsefSendInvoiceOnlineResponse(
    val referenceNumber: String,
)

@Serializable
data class KsefSessionStatusResponse(
    val status: KsefStatusInfo,
    val invoiceCount: Int? = null,
    val successfulInvoiceCount: Int? = null,
    val failedInvoiceCount: Int? = null,
)

@Serializable
data class KsefSessionInvoiceStatusResponse(
    val referenceNumber: String? = null,
    val invoiceNumber: String? = null,
    val ksefNumber: String? = null,
    val status: KsefStatusInfo? = null,
    val permanentStorageDate: String? = null,
)

@Serializable
data class KsefSendInvoiceResponse(
    val sessionReferenceNumber: String,
    val invoiceReferenceNumber: String,
    val ksefNumber: String? = null,
    val invoiceNumber: String? = null,
    @kotlinx.serialization.Transient
    val sessionStatus: KsefSessionInvoiceStatusResponse? = null,
)
