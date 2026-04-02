package com.kontenery.library.model.finance

import com.kontenery.library.model.PaymentForFinanceTable
import kotlinx.serialization.Serializable

@Serializable
data class TableRowFinance(
    val name: String,
    val values: Map<String, PaymentForFinanceTable?> // month -> amount
)