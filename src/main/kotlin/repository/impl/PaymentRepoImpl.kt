package com.kontenery.repository.impl

import com.kontenery.library.model.Payment
import com.kontenery.repository.PaymentRepo
import com.kontenery.repository.entity.*
import com.kontenery.repository.entity.invoice.InvoiceEntity
import com.kontenery.repository.entity.invoice.InvoiceTable
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PaymentRepoImpl: PaymentRepo {
    override suspend fun getPaymentsByClient(
        page: Int,
        size: Int,
        clientId: Long,
        from: LocalDate,
        to: LocalDate
    ): List<Payment> {
        val countOffset: Long = (page * size).toLong()
        return newSuspendedTransaction {
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
    }

    override suspend fun createPayment(payment: Payment): Payment = suspendTransaction {
        assert(payment.fromClient?.id != null)
        val clientEntity: ClientEntity? = payment.fromClient?.id?.let { ClientEntity.findById(it) }
        assert(clientEntity != null)

        val invoicesNumber: List<String> = payment.forInvoices.map { it.invoiceNumber!! }
        val invoices: SizedIterable<InvoiceEntity> = InvoiceEntity.find { InvoiceTable.invoiceNumber inList invoicesNumber }

//        println("payment in createPayment: $payment")

        PaymentEntity.new {
            amount = payment.amount
            date = payment.date
            fromClient = clientEntity!!
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
}