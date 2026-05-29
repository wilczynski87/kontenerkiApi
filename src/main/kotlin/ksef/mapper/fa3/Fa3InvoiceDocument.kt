package com.kontenery.ksef.mapper.fa3

import kotlinx.datetime.LocalDate

/**
 * Logical FA(3) document — mirrors sections of the official XML template used by CIRFMF SDK samples.
 */
data class Fa3InvoiceDocument(
    val header: Fa3Header,
    val seller: Fa3Party,
    val buyer: Fa3Buyer,
    val body: Fa3Body,
)

data class Fa3Header(
    val issueDate: LocalDate,
    val productionDateTime: String,
)

data class Fa3Party(
    val nip: String,
    val name: String,
    val address: Fa3Address,
    val email: String,
    val phoneDigits: String?,
)

data class Fa3Buyer(
    val nip: String,
    val name: String,
    val address: Fa3Address,
    val email: String,
    val phoneDigits: String?,
    val clientNumber: String?,
    val jst: String = Fa3Constants.JST_NOT_APPLICABLE,
    val gv: String = Fa3Constants.GV_NOT_APPLICABLE,
)

data class Fa3Address(
    val countryCode: String,
    val line1: String,
    val line2: String,
)

data class Fa3Body(
    val issueDate: LocalDate,
    val invoiceNumber: String,
    val saleDate: LocalDate,
    val vatSummaries: List<Fa3VatSummarySlot>,
    val grossTotal: String,
    val lines: List<Fa3Line>,
    val payment: Fa3Payment,
)

data class Fa3VatSummarySlot(
    val slotIndex: Int,
    val netAmount: String,
    val vatAmount: String?,
    val vatRateKey: String,
)

data class Fa3Line(
    val rowNumber: Int,
    val lineUuid: String,
    val productName: String,
    val unit: String,
    val quantity: String,
    val unitPriceNet: String,
    val netAmount: String,
    val vatRate: String,
)

data class Fa3Payment(
    val paid: String,
    val dueDate: LocalDate,
    val paymentForm: String,
    val bankAccountDigits: String?,
)
