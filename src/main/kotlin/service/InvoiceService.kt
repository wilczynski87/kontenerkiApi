package com.kontenery.service

import com.kontenery.model.invoice.Invoice
import com.kontenery.repository.endOfCurrentMonth
import com.kontenery.repository.endOfCurrentYear
import com.kontenery.repository.startOfCurrentMonth
import com.kontenery.repository.startOfCurrentYear
import kotlinx.datetime.LocalDate

interface InvoiceService {
    suspend fun getInvoicesForDate(page:Int = 0, size:Int = 100, from: LocalDate = LocalDate.startOfCurrentMonth(), to: LocalDate = LocalDate.endOfCurrentMonth()): List<Invoice>

    suspend fun getInvoicesForClient(page:Int = 0, size:Int = 100, clientId:Long, from: LocalDate = LocalDate.startOfCurrentYear(), to: LocalDate = LocalDate.endOfCurrentYear()): List<Invoice>

    suspend fun getInvoiceById(invoiceId:Long): Invoice?

    suspend fun saveInvoice(invoice: Invoice): Invoice?
}