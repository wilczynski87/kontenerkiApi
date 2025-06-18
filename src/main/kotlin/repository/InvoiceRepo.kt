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

    suspend fun saveInvoice(invoice: Invoice): Invoice?

    suspend fun getLastInvoiceNumber(): String?

    suspend fun getLastBillNumber(): String?

    suspend fun confirmInvoiceSendDate(invoiceNumber:String, date:LocalDate): Boolean
}

//fun LocalDate.Companion.now(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
//
//fun LocalDate.Companion.startOfCurrentMonth(period: LocalDate? = null): LocalDate {
//    val currentDate:LocalDate = period ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
//    return LocalDate(currentDate.year, currentDate.monthNumber, 1)
//}
//
//fun LocalDate.Companion.endOfCurrentMonth(period: LocalDate? = null): LocalDate {
//    val startOfCurrentMonth:LocalDate = if(period == null) LocalDate.Companion.startOfCurrentMonth() else LocalDate.Companion.startOfCurrentMonth(period)
//    return startOfCurrentMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
//}
//
//fun LocalDate.Companion.startOfCurrentYear(period: LocalDate? = null): LocalDate {
//    val currentDate: LocalDate = period ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
//    return parse("${currentDate.year}-01-01")
//}
//
//fun LocalDate.Companion.endOfCurrentYear(period: LocalDate? = null): LocalDate {
//    val endOfCurrentMonth: LocalDate = if(period == null) LocalDate.Companion.endOfCurrentMonth() else LocalDate.Companion.endOfCurrentMonth(period)
//    return parse("${endOfCurrentMonth.year}-12-${endOfCurrentMonth.dayOfMonth}")
//}