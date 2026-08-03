package com.kontenery.p24.service

import com.kontenery.p24.dto.P24CreateTransactionRequest
import com.kontenery.p24.dto.P24CreateTransactionResponse
import com.kontenery.p24.dto.P24NotificationPayload
import com.kontenery.p24.dto.P24TransactionStatusResponse

interface P24Service {
    suspend fun createTransaction(request: P24CreateTransactionRequest): P24CreateTransactionResponse
    suspend fun getTransaction(sessionId: String): P24TransactionStatusResponse
    /** Handles P24 urlStatus webhook. Returns true when processed (incl. idempotent replay). */
    suspend fun handleNotification(payload: P24NotificationPayload): Boolean
}
