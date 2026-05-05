package com.kontenery.service

import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.InvoiceSend

interface PrintService {

    suspend fun sendPeriodicInvoice(invoice:Invoice)

    suspend fun printInvoices(invoices: List<Invoice>)

    suspend fun sendUtilitiesInvoice(invoice:Invoice)

    suspend fun sendPeriodicBill(invoice: Invoice)

    suspend fun sendInvoiceAgain(invoice: Invoice): InvoiceSend
}