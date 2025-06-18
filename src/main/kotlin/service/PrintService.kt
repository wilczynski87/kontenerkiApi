package com.kontenery.service

import com.kontenery.library.model.invoice.Invoice
import kotlinx.datetime.LocalDate

interface PrintService {

    suspend fun sendPeriodicInvoice(invoice:Invoice)

    suspend fun printInvoices(from: LocalDate, to: LocalDate)

    suspend fun sendUtilitiesInvoice(invoice:Invoice)

    suspend fun sendPeriodicBill(invoice: Invoice)
}