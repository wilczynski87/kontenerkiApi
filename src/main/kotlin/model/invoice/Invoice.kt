package com.kontenery.model.invoice

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


data class Invoice(
    val invoiceNumber:String,
    val invoiceTitle:String = "Faktura VAT",
    val invoiceDate: LocalDate,
    val seller: Subject.Seller,
    val customer: Subject.Customer,
    val products:List<Position>,
    val vatAmountSum:String,
    val priceSum:String,
    val priceWithVatSum:String,
    val paymentDay:LocalDate,
    var mainAccount:String = "50 1950 0001 2006 0023 6241 0001"
)
