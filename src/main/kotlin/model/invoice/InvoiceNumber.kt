package com.kontenery.model.invoice

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


data class InvoiceNumber(
    var number: Long,
    val month: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber,
    val year: String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year.toString(),
) {
    fun toInvoiceNumberString():String {
        return "$number/$month/$year"
    }

    companion object {
        fun toInvoiceNumber(invoiceNumberString: String): InvoiceNumber {
            val number = invoiceNumberString.substringBefore("/").toLong()
            val month = invoiceNumberString.substringAfter("/").substringBefore("/").toInt()
            // Bills use trailing "r" (e.g. 26/8/2026r) — strip it so year stays numeric
            val year: String = invoiceNumberString.substringAfterLast("/").trimEnd('r', 'R')
            return InvoiceNumber(number, month, year)
        }
        fun getNumberFromInvoiceNumber(invoiceNumberString: String): Long {
            return invoiceNumberString.substringBefore("/").toLong()
        }
    }
}