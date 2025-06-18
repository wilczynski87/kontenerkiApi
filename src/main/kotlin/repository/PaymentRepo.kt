package com.kontenery.repository

import com.kontenery.library.model.Payment

interface PaymentRepo {
    suspend fun getPaymentsByClient(page: Int, size: Int, clientId: Long): List<Payment>
    suspend fun createPayment(payment: Payment): Payment
    suspend fun updatePayment(payment: Payment): Payment
    suspend fun readPaymentsFromStatement(): List<Payment>
}