package com.kontenery.data.finance

import com.kontenery.data.serializers.LocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ClientFinanceDto(
    val clientId: Long? = null,
    @Serializable(LocalDateSerializer::class)
    val from: LocalDate? = null,
    @Serializable(LocalDateSerializer::class)
    val to: LocalDate? = null,
    val income: Double? = null,
    val documentBalance: Double? = null,
    val totalBalance: Double? = null,
)
