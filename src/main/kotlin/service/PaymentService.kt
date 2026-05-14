package com.kontenery.service

import com.kontenery.data.Payment
import com.kontenery.data.PaymentDto
import kotlinx.datetime.LocalDate

interface PaymentService {
    suspend fun getPaymentsByClient(page: Int = 0, size: Int = 100, clientId: Long, from: LocalDate, to: LocalDate): List<Payment>
    suspend fun createPayment(paymentDto: PaymentDto): Payment
    suspend fun updatePayment(paymentDto: PaymentDto): Payment
    suspend fun deletePayment(paymentId: Long):Boolean
    suspend fun readPaymentsFromStatement(): List<Payment>
    suspend fun clientOverdue(clientId: Long, from: LocalDate, to: LocalDate): Double
}