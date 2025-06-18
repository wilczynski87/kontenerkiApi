package com.kontenery.service.impl

import com.kontenery.library.model.Payment
import com.kontenery.library.model.PaymentDto
import com.kontenery.repository.PaymentRepo
import com.kontenery.service.PaymentService

class PaymentServiceImpl(paymentRepo: PaymentRepo): PaymentService {
    override suspend fun getPaymentsByClient(page: Int, size: Int, clientId: Long): List<Payment> {
        TODO("Not yet implemented")
    }

    override suspend fun createPayment(payment: PaymentDto): Payment {
        TODO("Not yet implemented")
    }

    override suspend fun updatePayment(payment: PaymentDto): Payment {
        TODO("Not yet implemented")
    }

    override suspend fun deletePayment(paymentId: Long): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun readPaymentsFromStatement(): List<Payment> {
        TODO("Not yet implemented")
    }

    private fun dtoToPayment(dto: PaymentDto): Payment {
        TODO("Not yet implemented")
    }
}