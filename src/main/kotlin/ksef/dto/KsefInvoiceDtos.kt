package com.kontenery.ksef.dto

import kotlinx.serialization.Serializable

@Serializable
data class KsefInvoiceQueryDateRange(
    val dateType: String,
    val from: String,
    val to: String,
)

@Serializable
data class KsefInvoiceQueryFilters(
    val subjectType: String,
    val dateRange: KsefInvoiceQueryDateRange,
    val ksefNumber: String? = null,
    val invoiceNumber: String? = null,
)

@Serializable
data class KsefInvoiceMetadataSeller(
    val nip: String? = null,
    val name: String? = null,
)

@Serializable
data class KsefInvoiceMetadataBuyer(
    val identifier: String? = null,
    val name: String? = null,
)

@Serializable
data class KsefInvoiceMetadata(
    val ksefNumber: String? = null,
    val invoiceNumber: String? = null,
    val invoicingDate: String? = null,
    val issueDate: String? = null,
    val acquisitionDate: String? = null,
    val permanentStorageDate: String? = null,
    val seller: KsefInvoiceMetadataSeller? = null,
    val buyer: KsefInvoiceMetadataBuyer? = null,
    val netAmount: Double? = null,
    val grossAmount: Double? = null,
    val vatAmount: Double? = null,
    val currency: String? = null,
    val hasAttachment: Boolean? = null,
)

@Serializable
data class KsefQueryInvoiceMetadataResponse(
    val hasMore: Boolean? = null,
    val isTruncated: Boolean? = null,
    val invoices: List<KsefInvoiceMetadata> = emptyList(),
    val permanentStorageHwmDate: String? = null,
)

@Serializable
data class KsefInvoiceListResponse(
    val invoices: List<KsefInvoiceMetadata>,
    val hasMore: Boolean,
    val pageOffset: Int,
    val pageSize: Int,
)

@Serializable
data class KsefDownloadedInvoice(
    val ksefNumber: String,
    val invoiceNumber: String? = null,
    val xml: String,
)

@Serializable
data class KsefDownloadInvoiceResponse(
    val ksefNumber: String,
    val invoiceNumber: String? = null,
    val xml: String,
)

@Serializable
data class KsefDownloadInvoicesMonthResponse(
    val year: Int,
    val month: Int,
    val downloadedCount: Int,
    val invoices: List<KsefDownloadedInvoice>,
    val skippedWithoutKsefNumber: Int = 0,
)
