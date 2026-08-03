package com.kontenery.repository

import com.kontenery.data.p24.P24Transaction
import com.kontenery.p24.dto.P24TransactionStatus

interface P24TransactionRepo {
    suspend fun create(transaction: P24Transaction): P24Transaction
    suspend fun findBySessionId(sessionId: String): P24Transaction?
    suspend fun updateStatus(
        sessionId: String,
        status: P24TransactionStatus,
        token: String? = null,
        orderId: Long? = null,
        methodId: Int? = null,
        statement: String? = null,
        paymentId: Long? = null,
        errorMessage: String? = null,
    ): P24Transaction?
}
