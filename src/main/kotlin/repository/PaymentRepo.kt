package com.kontenery.repository

import com.kontenery.data.Payment
import kotlinx.datetime.LocalDate

interface PaymentRepo {
    suspend fun getPaymentsByClient(page: Int = 0, size: Int = 100, clientId: Long, from: LocalDate, to: LocalDate): List<Payment>
    suspend fun findById(paymentId: Long): Payment?
    suspend fun createPayment(payment: Payment): Payment
    /** Tworzy obie strony zaliczenia w jednej transakcji DB. */
    suspend fun createTransferPair(debit: Payment, credit: Payment): Pair<Payment, Payment>
    suspend fun updatePayment(payment: Payment): Payment
    suspend fun readPaymentsFromStatement(): List<Payment>
    suspend fun isPaymentWithReferenceNr(referenceNumber: String): Boolean
    suspend fun isDuplicate(newPayment: Payment): Boolean
    suspend fun deletePayment(paymentId: Long): Boolean
}