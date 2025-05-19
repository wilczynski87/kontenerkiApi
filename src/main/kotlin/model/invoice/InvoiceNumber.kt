package com.kontenery.model.invoice

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


data class InvoiceNumber(
    var number: Long,
    var month: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).monthNumber,
    var year: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year,
) {
    fun toInvoiceNumberString():String {
        return "$number/$month/$year"
    }

    companion object {
        fun toInvoiceNumber(invoiceNumberString: String): InvoiceNumber {
            val number = invoiceNumberString.substringBefore("/").toLong()
            val month = invoiceNumberString.substringAfter("/").substringBefore("/").toInt()
            val year = invoiceNumberString.substringAfterLast("/").toInt()
            return InvoiceNumber(number, month, year)
        }
    }
}