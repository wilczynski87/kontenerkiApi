package com.kontenery.utils

import com.kontenery.model.invoice.InvoiceNumber
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object InvoiceLastNumber {
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    lateinit var InvoiceNumber: InvoiceNumber
}