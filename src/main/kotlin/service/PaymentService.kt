package com.kontenery.service

import com.kontenery.library.model.Payment
import com.kontenery.library.model.PaymentDto
import com.kontenery.library.utils.errors.PaymentError
import kotlinx.datetime.LocalDate

interface PaymentService {
    suspend fun getPaymentsByClient(page: Int = 0, size: Int = 100, clientId: Long, from: LocalDate, to: LocalDate): List<Payment>
    suspend fun createPayment(paymentDto: PaymentDto): Payment
    suspend fun updatePayment(paymentDto: PaymentDto): Payment
    suspend fun deletePayment(paymentId: Long):Boolean
    suspend fun readPaymentsFromStatement(): List<Payment>
    suspend fun clientOverdue(clientId: Long, from: LocalDate, to: LocalDate): Double
    // If validatePayment is TRUE -> there is no payment in db, we can proceed further
    suspend fun validatePayment(newPayment: Payment, errors: MutableList<PaymentError>? = null): Boolean
    suspend fun validatePaymentByParams(newPayment: Payment, errors: MutableList<PaymentError>? = null): Boolean
}