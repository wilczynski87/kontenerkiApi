package com.kontenery.service

import com.kontenery.library.model.Payment
import com.kontenery.library.model.PaymentDto

interface PaymentService {
    suspend fun getPaymentsByClient(page: Int, size: Int, clientId: Long): List<Payment>
    suspend fun createPayment(payment: PaymentDto): Payment
    suspend fun updatePayment(payment: PaymentDto): Payment
    suspend fun deletePayment(paymentId: Long):Boolean
    suspend fun readPaymentsFromStatement(): List<Payment>
}