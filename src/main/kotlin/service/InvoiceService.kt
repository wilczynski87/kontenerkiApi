package com.kontenery.service

import com.kontenery.data.Client
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.utils.endOfCurrentMonth
import com.kontenery.data.utils.endOfCurrentYear
import com.kontenery.data.utils.errors.ErrorMessage
import com.kontenery.data.utils.startOfCurrentMonth
import com.kontenery.data.utils.startOfCurrentYear
import kotlinx.datetime.LocalDate

interface InvoiceService {
    suspend fun getInvoicesForDate(page:Int = 0, size:Int = 100, from: LocalDate = LocalDate.startOfCurrentMonth(), to: LocalDate = LocalDate.endOfCurrentMonth()): List<Invoice>

    suspend fun getInvoicesAndBillsForClient(page:Int = 0, size:Int = 100, clientId:Long, from: LocalDate = LocalDate.startOfCurrentYear(), to: LocalDate = LocalDate.endOfCurrentYear()): List<Invoice>

    suspend fun getInvoiceByNumber(invoiceNumber: String): Invoice?

    suspend fun saveInvoice(invoice: Invoice): Invoice?

    suspend fun saveInvoiceWithErrors(
        isInvoice: Boolean,
        invoice: Invoice,
        errors: MutableList<ErrorMessage>
    ): Invoice?

    suspend fun createPeriodicInvoiceForClient(
        client: Client,
        period: LocalDate?,
        errorList: MutableList<ErrorMessage>
    ): Invoice?

    /** True if client already has a PERIODIC invoice/bill for the given month. */
    suspend fun hasPeriodicDocumentForClient(
        clientId: Long,
        period: LocalDate,
        vatApply: Boolean,
    ): Boolean

    // przerobić save Invoice
    suspend fun createCustomInvoice(invoice: Invoice): Invoice?

    suspend fun createUtilitiesInvoice(invoice: Invoice)

    suspend fun confirmInvoiceSendDate(invoiceNumber:String, date:LocalDate): Boolean
}