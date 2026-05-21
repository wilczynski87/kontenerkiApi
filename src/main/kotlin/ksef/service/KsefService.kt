package com.kontenery.ksef.service

import com.kontenery.data.invoice.Invoice
import com.kontenery.ksef.dto.KsefInvoiceListResponse
import com.kontenery.ksef.dto.KsefLoginResponse
import com.kontenery.ksef.dto.KsefSendInvoiceResponse

interface KsefService {
    suspend fun login(): KsefLoginResponse
    suspend fun listInvoices(
        from: String? = null,
        to: String? = null,
        pageOffset: Int = 0,
        pageSize: Int = 50,
        subjectType: String = "Subject1",
    ): KsefInvoiceListResponse
    suspend fun sendInvoice(invoice: Invoice): KsefSendInvoiceResponse
    suspend fun sendInvoiceById(invoiceId: Long): KsefSendInvoiceResponse
}
