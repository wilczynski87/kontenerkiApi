package com.kontenery.data.p24

import com.kontenery.p24.dto.P24TransactionStatus
import kotlinx.serialization.Serializable

@Serializable
data class P24Transaction(
    val id: Long? = null,
    val sessionId: String,
    val clientId: Long,
    val amountGrosze: Int,
    val currency: String = "PLN",
    val description: String? = null,
    val email: String,
    val invoiceNumbers: List<String> = emptyList(),
    val urlReturn: String,
    val status: P24TransactionStatus = P24TransactionStatus.PENDING,
    val token: String? = null,
    val orderId: Long? = null,
    val methodId: Int? = null,
    val statement: String? = null,
    val paymentId: Long? = null,
    val errorMessage: String? = null,
)
