package com.kontenery.ksef.service

import com.kontenery.ksef.dto.KsefInvoiceListResponse
import com.kontenery.ksef.dto.KsefLoginResponse

interface KsefService {
    suspend fun login(): KsefLoginResponse
    suspend fun listInvoices(
        from: String? = null,
        to: String? = null,
        pageOffset: Int = 0,
        pageSize: Int = 50,
        subjectType: String = "Subject1",
    ): KsefInvoiceListResponse
}
