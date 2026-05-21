package com.kontenery.data.ksef

import kotlinx.serialization.Serializable

@Serializable
data class KsefSessionInvoiceStatus(
    val id: Long? = null,
    val invoiceId: Long,
    val referenceNumber: String? = null,
    val invoiceNumber: String? = null,
    val ksefNumber: String? = null,
    val statusCode: Int? = null,
    val statusDescription: String? = null,
    val permanentStorageDate: String? = null,
)
