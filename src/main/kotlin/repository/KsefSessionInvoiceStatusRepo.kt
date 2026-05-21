package com.kontenery.repository

import com.kontenery.data.ksef.KsefSessionInvoiceStatus
import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse

interface KsefSessionInvoiceStatusRepo {
    suspend fun save(invoiceId: Long, status: KsefSessionInvoiceStatusResponse): KsefSessionInvoiceStatus

    suspend fun getLatestByInvoiceId(invoiceId: Long): KsefSessionInvoiceStatus?

    suspend fun getAllByInvoiceId(invoiceId: Long): List<KsefSessionInvoiceStatus>
}
