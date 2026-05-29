package com.kontenery.ksef.service

import com.kontenery.data.invoice.Invoice
import com.kontenery.ksef.dto.KsefDownloadInvoiceResponse
import com.kontenery.ksef.dto.KsefDownloadInvoicesMonthResponse
import com.kontenery.ksef.dto.KsefInvoiceListResponse
import com.kontenery.ksef.dto.KsefInvoiceRegisteredResponse
import com.kontenery.ksef.dto.KsefLoginResponse
import com.kontenery.ksef.dto.KsefSendInvoiceResponse
import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse

interface KsefService {
    suspend fun login(): KsefLoginResponse
    suspend fun listInvoicesKsef(
        from: String? = null,
        to: String? = null,
        pageOffset: Int = 0,
        pageSize: Int = 50,
        subjectType: String = "Subject1",
    ): KsefInvoiceListResponse
    suspend fun downloadInvoiceFromKsef(
        ksefNumber: String? = null,
        invoiceNumber: String? = null,
        subjectType: String = "Subject1",
    ): KsefDownloadInvoiceResponse
    suspend fun downloadInvoicesForMonthFromKsef(
        year: Int,
        month: Int,
        subjectType: String = "Subject1",
    ): KsefDownloadInvoicesMonthResponse
    suspend fun isInvoiceRegisteredInKsef(
        invoiceNumber: String,
        subjectType: String = "Subject1",
    ): KsefInvoiceRegisteredResponse
    suspend fun sendInvoiceToKsef(invoice: Invoice): KsefSendInvoiceResponse
    suspend fun sendInvoiceToKsefByNumber(invoiceNumber: String): KsefSendInvoiceResponse
    suspend fun persistSessionStatus(invoiceNumber: String, status: KsefSessionInvoiceStatusResponse)
}
