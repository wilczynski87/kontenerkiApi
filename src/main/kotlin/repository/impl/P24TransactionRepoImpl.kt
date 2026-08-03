package com.kontenery.repository.impl

import com.kontenery.data.p24.P24Transaction
import com.kontenery.p24.dto.P24TransactionStatus
import com.kontenery.repository.P24TransactionRepo
import com.kontenery.repository.entity.PaymentTable
import com.kontenery.repository.entity.suspendTransaction
import com.kontenery.repository.entity.p24.P24TransactionEntity
import com.kontenery.repository.entity.p24.P24TransactionTable
import com.kontenery.repository.entity.p24.encodeInvoiceNumbers
import org.jetbrains.exposed.dao.id.EntityID

class P24TransactionRepoImpl : P24TransactionRepo {
    override suspend fun create(transaction: P24Transaction): P24Transaction = suspendTransaction {
        P24TransactionEntity.new {
            sessionId = transaction.sessionId
            clientId = EntityID(transaction.clientId, com.kontenery.repository.entity.ClientTable)
            amountGrosze = transaction.amountGrosze
            currency = transaction.currency
            description = transaction.description
            email = transaction.email
            invoiceNumbers = encodeInvoiceNumbers(transaction.invoiceNumbers)
            urlReturn = transaction.urlReturn
            status = transaction.status
            token = transaction.token
            orderId = transaction.orderId
            methodId = transaction.methodId
            statement = transaction.statement
            paymentId = transaction.paymentId?.let { EntityID(it, PaymentTable) }
            errorMessage = transaction.errorMessage
        }.toDomain()
    }

    override suspend fun findBySessionId(sessionId: String): P24Transaction? = suspendTransaction {
        P24TransactionEntity.find { P24TransactionTable.sessionId eq sessionId }
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun updateStatus(
        sessionId: String,
        status: P24TransactionStatus,
        token: String?,
        orderId: Long?,
        methodId: Int?,
        statement: String?,
        paymentId: Long?,
        errorMessage: String?,
    ): P24Transaction? = suspendTransaction {
        val entity = P24TransactionEntity.find { P24TransactionTable.sessionId eq sessionId }
            .firstOrNull()
            ?: return@suspendTransaction null
        entity.status = status
        if (token != null) entity.token = token
        if (orderId != null) entity.orderId = orderId
        if (methodId != null) entity.methodId = methodId
        if (statement != null) entity.statement = statement
        if (paymentId != null) entity.paymentId = EntityID(paymentId, PaymentTable)
        if (errorMessage != null) entity.errorMessage = errorMessage
        entity.toDomain()
    }
}
