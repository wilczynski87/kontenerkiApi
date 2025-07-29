package com.kontenery.service

import com.kontenery.library.model.Payment
import com.kontenery.library.model.PaymentDto
import kotlinx.datetime.LocalDate

interface PaymentService {
    suspend fun getPaymentsByClient(page: Int = 0, size: Int = 100, clientId: Long, from: LocalDate, to: LocalDate): List<Payment>
    suspend fun createPayment(payment: PaymentDto): Payment
    suspend fun updatePayment(payment: PaymentDto): Payment
    suspend fun deletePayment(paymentId: Long):Boolean
    suspend fun readPaymentsFromStatement(): List<Payment>
    suspend fun clientOverdue(clientId: Long, from: LocalDate, to: LocalDate): Double
}