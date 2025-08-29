package com.kontenery.service

import com.kontenery.library.model.invoice.Invoice
import kotlinx.datetime.LocalDate

interface PrintService {

    suspend fun sendPeriodicInvoice(invoice:Invoice)

    suspend fun printInvoices(invoices: List<Invoice>)

    suspend fun sendUtilitiesInvoice(invoice:Invoice)

    suspend fun sendPeriodicBill(invoice: Invoice)

    suspend fun sendInvoiceAgain(invoice: Invoice)
}