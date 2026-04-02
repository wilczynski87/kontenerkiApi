package com.kontenery.repository

import com.kontenery.data.invoice.Invoice
import com.kontenery.data.utils.endOfCurrentMonth
import com.kontenery.data.utils.endOfCurrentYear
import com.kontenery.data.utils.startOfCurrentMonth
import com.kontenery.data.utils.startOfCurrentYear
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

    suspend fun getBillByNumber(billNumber: String): Invoice?

    suspend fun getLastBillNumber(): String?

    suspend fun getLastBillForClient(clientId:Long): Invoice?

    suspend fun confirmBillSendDate(invoiceNumber:String, date:LocalDate): Boolean
}