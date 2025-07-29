package com.kontenery.repository

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.endOfCurrentMonth
import com.kontenery.library.utils.endOfCurrentYear
import com.kontenery.library.utils.startOfCurrentMonth
import com.kontenery.library.utils.startOfCurrentYear
import kotlinx.datetime.*

interface InvoiceRepo {
    suspend fun getInvoicesForDate(page:Int = 0, size:Int = 100, from: LocalDate = LocalDate.startOfCurrentMonth(), to:LocalDate = LocalDate.endOfCurrentMonth()): List<Invoice>

    suspend fun getInvoicesForClient(page:Int = 0, size:Int = 100, clientId:Long, from: LocalDate = LocalDate.startOfCurrentYear(), to:LocalDate = LocalDate.endOfCurrentYear()): List<Invoice>

    suspend fun getInvoiceById(invoiceId:Long): Invoice?

    suspend fun getInvoiceByNumber(invoiceNumber: String): Invoice?

    suspend fun saveInvoice(invoice: Invoice): Invoice?

    suspend fun getLastInvoiceNumber(): String?

    suspend fun getLastBillNumber(): String?

    suspend fun getLastInvoiceForClient(clientId: Long): Invoice?

    suspend fun confirmInvoiceSendDate(invoiceNumber:String, date:LocalDate): Boolean
}