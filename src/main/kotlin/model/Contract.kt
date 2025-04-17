package com.kontenery.model

import com.kontenery.utils.BigDecimalSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Contract(
    val id: Long? = null,
    var client: Client? = null,
    var product: Product? = null,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    @Serializable(with = BigDecimalSerializer::class)
    var netPrice: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val vatRate: BigDecimal = BigDecimal(23),
    var needInvoice: Boolean? = null,
)

@Serializable
data class ContractDto(
    val id: Long? = null,
    var client: Long? = null,
    var product: Long? = null,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    @Serializable(with = BigDecimalSerializer::class)
    var netPrice: BigDecimal? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val vatRate: BigDecimal = BigDecimal(23),
    var needInvoice: Boolean? = null,
)