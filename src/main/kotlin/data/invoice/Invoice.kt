package com.kontenery.data.invoice

import com.kontenery.data.serializers.LocalDateSerializer
import com.kontenery.data.utils.InvoiceType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Invoice(
    val invoiceNumber:String? = null,
    val invoiceTitle:String? = "Faktura VAT",
    @Serializable(with = LocalDateSerializer::class)
    val invoiceDate: LocalDate? = null,
    val seller: Subject.Seller? = null,
    val customer: Subject.Customer? = null,
    val products:List<Position> = mutableListOf(),
    val vatAmountSum:String? = null,
    val priceSum:String? = null,
    val priceWithVatSum:String? = null,
    @Serializable(with = LocalDateSerializer::class)
    val paymentDay:LocalDate? = null,
    val mainAccount:String = "50 1950 0001 2006 0023 6241 0001",
    @Serializable(with = LocalDateSerializer::class)
    val invoiceSendToClient: LocalDate? = null,
    val type: String? = InvoiceType.PERIODIC.name,
    val vatApply: Boolean = false,
) {
}

@Serializable
data class InvoiceSend(
    val invoiceNumber: String? = null,
    val forClient: String? = null,
    @Serializable(with = LocalDateSerializer::class)
    val sendFirstTime: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
    val sendLastTime: LocalDate? = null,
)