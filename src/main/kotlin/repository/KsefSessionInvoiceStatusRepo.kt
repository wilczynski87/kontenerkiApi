package com.kontenery.repository

import com.kontenery.data.ksef.KsefSessionInvoiceStatus
import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse

interface KsefSessionInvoiceStatusRepo {
    suspend fun save(invoiceNumber: String, status: KsefSessionInvoiceStatusResponse): KsefSessionInvoiceStatus

    suspend fun getLatestByInvoiceNumber(invoiceNumber: String): KsefSessionInvoiceStatus?

    suspend fun getAllByInvoiceId(invoiceId: Long): List<KsefSessionInvoiceStatus>
}
