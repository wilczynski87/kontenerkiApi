package com.kontenery.repository.impl

import com.kontenery.library.model.Payment
import com.kontenery.repository.PaymentRepo

class PaymentRepoImpl: PaymentRepo {
    override suspend fun getPaymentsByClient(page: Int, size: Int, clientId: Long): List<Payment> {
        TODO("Not yet implemented")
    }

    override suspend fun createPayment(payment: Payment): Payment {
        TODO("Not yet implemented")
    }

    override suspend fun updatePayment(payment: Payment): Payment {
        TODO("Not yet implemented")
    }

    override suspend fun readPaymentsFromStatement(): List<Payment> {
        TODO("Not yet implemented")
    }
}