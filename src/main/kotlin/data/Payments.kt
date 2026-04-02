package com.kontenery.data

import com.kontenery.data.invoice.Invoice
import com.kontenery.data.serializers.BigDecimalSerializer
import com.kontenery.data.serializers.LocalDateSerializer
import com.kontenery.data.utils.SellerAccount
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Payment(
    val id: Long? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val fromClient: Client? = null,
    val method: String? = null,
    val toAccount: SellerAccount? = SellerAccount.BUSSINESS,
    val fromAccount: String? = null,
    val title: String? = null,
    val forInvoices: List<Invoice> = listOf(),
    val referenceNumber: String? = null

    ) {

    fun toDto(): PaymentDto {
        return PaymentDto(
            paymentId = this.id.toString(),
            amount = this.amount.toDouble(),
            date = this.date,
            fromClientId = this.fromClient?.id,
            method = this.method,
            toAccount = this.toAccount?.accountNumber,
            fromAccount = this.fromAccount,
            title = this.title,
            forInvoices = this.forInvoices.mapNotNull { it.invoiceNumber },
            referenceNumber = this.referenceNumber
        )
    }
}

@Serializable
data class PaymentDto(
    val paymentId: String? = null,
    val amount: Double? = null,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate? = null,
    val fromClientId: Long? = null,
    val method: String? = null,
    val toAccount: String? = null,
    val fromAccount: String? = null,
    val title: String? = null,
    val forInvoices: List<String>? = null,
    val referenceNumber: String? = null,
)

@Serializable
enum class PaymentMethod(val polishName: String) {
    TRANSFER("przelew"),
    BLIK("blik"),
    CASH("gotówka"),
    OTHER("inna");

    companion object {
        fun fromName(polishName: String): PaymentMethod? = entries.find {
            it.polishName == polishName
        }
    }
}

@Serializable
data class PaymentForFinanceTable(
    val paymentId: Long? = null,
    val date: String? = "",   // np. "2026-01-10"
    val amount: Double? = null  // np. 284.55
)

@Serializable
data class PaymentsListForFinanceTable(
    val client: ClientOnListForFinance? = null,
    val payments: List<PaymentForFinanceTable> = listOf(),
)

@Serializable
data class PaymentsListForFinanceTableWithBalance(
    val client: ClientOnListForFinance? = null,
    val payments: List<PaymentForFinanceTable> = listOf(),
    val lastYearsBalance: Double? = null,
    val clientOverdue: Double? = null,
)