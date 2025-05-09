package com.kontenery.service.impl

import com.kontenery.model.invoice.Invoice
import com.kontenery.repository.InvoiceRepo
import com.kontenery.service.InvoiceService
import kotlinx.datetime.LocalDate

class InvoiceServiceImpl(private val invoiceRepo: InvoiceRepo): InvoiceService {
    override suspend fun getInvoicesForDate(page: Int, size: Int, from: LocalDate, to: LocalDate): List<Invoice> {
        return invoiceRepo.getInvoicesForDate(page, size, from, to)
    }

    override suspend fun getInvoicesForClient(
        page: Int,
        size: Int,
        clientId: Long,
        from: LocalDate,
        to: LocalDate
    ): List<Invoice> {
        return invoiceRepo.getInvoicesForClient(page, size, clientId, from, to)
    }

    override suspend fun getInvoiceById(invoiceId: Long): Invoice? {
        return invoiceRepo.getInvoiceById(invoiceId)
    }

    override suspend fun saveInvoice(invoice: Invoice): Invoice? {
        return invoiceRepo.saveInvoice(invoice)
    }
}