package com.kontenery.repository.impl

import com.kontenery.data.Payment
import com.kontenery.repository.PaymentRepo
import com.kontenery.repository.entity.*
import com.kontenery.repository.entity.invoice.InvoiceEntity
import com.kontenery.repository.entity.invoice.InvoiceTable
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PaymentRepoImpl: PaymentRepo {
    override suspend fun getPaymentsByClient(
        page: Int,
        size: Int,
        clientId: Long,
        from: LocalDate,
        to: LocalDate
    ): List<Payment> = newSuspendedTransaction {
        val countOffset: Long = (page * size).toLong()
        PaymentEntity
            .find {
                (PaymentTable.fromClient eq clientId) and
                (PaymentTable.date.between(from, to))
            }
            .limit(size)
            .offset(countOffset)
            .with(PaymentEntity::fromClient, PaymentEntity::forInvoices)
            .map { it.toDomain() }
    }

    override suspend fun findById(paymentId: Long): Payment? = suspendTransaction {
        PaymentEntity.findById(paymentId)
            ?.also { it.forInvoices.toList(); it.fromClient }
            ?.toDomain()
    }

    override suspend fun createPayment(payment: Payment): Payment = suspendTransaction {
        insertPayment(payment)
    }

    override suspend fun createTransferPair(
        debit: Payment,
        credit: Payment,
    ): Pair<Payment, Payment> = suspendTransaction {
        insertPayment(debit) to insertPayment(credit)
    }

    private fun insertPayment(payment: Payment): Payment {
        val clientId = payment.fromClient?.id
            ?: error("Payment requires fromClient.id")
        val clientEntity = ClientEntity.findById(clientId)
            ?: error("Client $clientId not found")

        val invoiceNumbers = payment.forInvoices.mapNotNull { it.invoiceNumber }
        val invoices = InvoiceEntity.find { InvoiceTable.invoiceNumber inList invoiceNumbers }

        return PaymentEntity.new {
            amount = payment.amount
            date = payment.date
            fromClient = clientEntity
            method = payment.method
            toAccount = payment.toAccount
            fromAccount = payment.fromAccount
            title = payment.title
            forInvoices = invoices
            referenceNumber = payment.referenceNumber
        }.toDomain()
    }

    override suspend fun updatePayment(payment: Payment): Payment = newSuspendedTransaction {
        TODO("Not yet implemented")
    }

    override suspend fun readPaymentsFromStatement(): List<Payment> = newSuspendedTransaction {
        TODO("Not yet implemented")
    }

    override suspend fun isPaymentWithReferenceNr(referenceNumber: String): Boolean = newSuspendedTransaction {
        PaymentEntity.find {
            PaymentTable.referenceNumber eq referenceNumber
        }.any()
    }

    override suspend fun isDuplicate(newPayment: Payment): Boolean = newSuspendedTransaction {
        PaymentEntity.find {
            (PaymentTable.amount eq newPayment.amount) and
            (PaymentTable.date eq newPayment.date) and
            (PaymentTable.title eq newPayment.title) and
            (PaymentTable.fromClient eq newPayment.fromClient?.id)
        }.any()
    }

    override suspend fun deletePayment(paymentId: Long): Boolean {
        return newSuspendedTransaction {
            PaymentEntity.findById(paymentId)?.delete() != null
        }
    }
}