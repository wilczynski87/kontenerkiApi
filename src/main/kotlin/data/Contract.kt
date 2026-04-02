package com.kontenery.data

import com.kontenery.data.serializers.BigDecimalSerializer
import com.kontenery.data.serializers.LocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Contract(
    val id: Long? = null,
    var client: Client? = null,
    var product: Product? = null,
    @Serializable(with = LocalDateSerializer::class)
    var startDate: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
    var endDate: LocalDate? = null,
    @Serializable(with = BigDecimalSerializer::class)
    var netPrice: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val vatRate: BigDecimal = BigDecimal(23),
    var needInvoice: Boolean? = null,
    var deposit: Deposit? = null,
) {
    fun toContractDTO(): ContractDto =
        ContractDto(
            id = id,
            client = client?.id,
            product = product?.id,
            startDate = startDate,
            endDate = endDate,
            netPrice = netPrice?.toDouble(),
            vatRate = vatRate.toDouble(),
            needInvoice = needInvoice,
            deposit = deposit
        )
}

@Serializable
data class ContractDto(
    val id: Long? = null,
    var client: Long? = null,
    var product: Long? = null,
    @Serializable(with = LocalDateSerializer::class)
    var startDate: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
    var endDate: LocalDate? = null,
    var netPrice: Double? = null,
    val vatRate: Double? = 23.00,
    var needInvoice: Boolean? = null,
    var deposit: Deposit? = null,
) {

}

@Serializable
data class Deposit(
    val type: DepositType? = null,
    val note: String? = null,
    val amount: String? = null
)

enum class DepositType(
    val displayName: String
) {
    CASH("Cash"),
    BILL_OF_EXCHANGE("Bill of exchange"),
    INSURANCE("Insurance"),
    NONE("None");
}