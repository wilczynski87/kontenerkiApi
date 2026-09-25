package com.kontenery.service

import com.kontenery.data.Payment
import com.kontenery.data.PaymentDto
import com.kontenery.data.PaymentTransferRequest
import kotlinx.datetime.LocalDate

interface PaymentService {
    suspend fun getPaymentsByClient(page: Int = 0, size: Int = 100, clientId: Long, from: LocalDate, to: LocalDate): List<Payment>
    suspend fun createPayment(paymentDto: PaymentDto): Payment
    suspend fun updatePayment(paymentDto: PaymentDto): Payment
    suspend fun deletePayment(paymentId: Long):Boolean
    suspend fun transferPayment(request: PaymentTransferRequest): PaymentTransferResult
    suspend fun readPaymentsFromStatement(): List<Payment>
    suspend fun clientOverdue(clientId: Long, from: LocalDate, to: LocalDate): Double
    suspend fun isDuplicated(newPayment: Payment): Boolean
}

sealed class PaymentTransferResult {
    data class Success(val debit: Payment, val credit: Payment) : PaymentTransferResult()
    data class NotFound(val message: String) : PaymentTransferResult()
    data class BadRequest(val message: String) : PaymentTransferResult()
}
