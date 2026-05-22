package com.kontenery.ksef.service

import com.kontenery.data.invoice.Invoice
import com.kontenery.ksef.dto.KsefInvoiceListResponse
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
    suspend fun sendInvoiceToKsef(invoice: Invoice): KsefSendInvoiceResponse
    suspend fun sendInvoiceToKsefByNumber(invoiceNumber: String): KsefSendInvoiceResponse
    suspend fun persistSessionStatus(invoiceNumber: String, status: KsefSessionInvoiceStatusResponse)
}
