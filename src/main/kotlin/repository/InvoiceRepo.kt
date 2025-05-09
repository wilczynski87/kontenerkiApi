package com.kontenery.repository

import com.kontenery.model.invoice.Invoice
import kotlinx.datetime.*

interface InvoiceRepo {
    suspend fun getInvoicesForDate(page:Int = 0, size:Int = 100, from: LocalDate = LocalDate.startOfCurrentMonth(), to:LocalDate = LocalDate.endOfCurrentMonth()): List<Invoice>

    suspend fun getInvoicesForClient(page:Int = 0, size:Int = 100, clientId:Long, from: LocalDate = LocalDate.startOfCurrentYear(), to:LocalDate = LocalDate.endOfCurrentYear()): List<Invoice>

    suspend fun getInvoiceById(invoiceId:Long): Invoice?

    suspend fun saveInvoice(invoice: Invoice): Invoice?
}

fun LocalDate.Companion.now(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

fun LocalDate.Companion.startOfCurrentMonth(): LocalDate {
    val currentDate:LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return parse("${currentDate.year}-${currentDate.month}-01")
}

fun LocalDate.Companion.endOfCurrentMonth(): LocalDate {
    val startOfCurrentMonth:LocalDate = LocalDate.Companion.startOfCurrentMonth()
    return parse("${startOfCurrentMonth.year}-${startOfCurrentMonth.month}-${startOfCurrentMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)}")
}

fun LocalDate.Companion.startOfCurrentYear(): LocalDate {
    val currentDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return parse("${currentDate.year}-01-01")
}

fun LocalDate.Companion.endOfCurrentYear(): LocalDate {
    val endOfCurrentMonth: LocalDate = LocalDate.Companion.endOfCurrentMonth()
    return parse("${endOfCurrentMonth.year}-12-${endOfCurrentMonth.dayOfMonth}")
}