package com.kontenery.repository

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.endOfCurrentMonth
import com.kontenery.library.utils.endOfCurrentYear
import com.kontenery.library.utils.startOfCurrentMonth
import com.kontenery.library.utils.startOfCurrentYear
import kotlinx.datetime.LocalDate

interface BillRepo {

    suspend fun saveBill(bill: Invoice): Invoice?

    suspend fun getBillForDate(
        page:Int = 0,
        size:Int = 100,
        from: LocalDate = LocalDate.startOfCurrentMonth(),
        to:LocalDate = LocalDate.endOfCurrentMonth()
    ): List<Invoice>

    suspend fun getBillsForClient(
        page:Int = 0,
        size:Int = 100,
        clientId:Long,
        from: LocalDate = LocalDate.startOfCurrentYear(),
        to:LocalDate = LocalDate.endOfCurrentYear()
    ): List<Invoice>

    suspend fun getBillById(invoiceId:Long): Invoice?

    suspend fun getLastBillNumber(): String?

    suspend fun confirmBillSendDate(invoiceNumber:String, date:LocalDate): Boolean
}