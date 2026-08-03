package com.kontenery.repository.entity.p24

import com.kontenery.data.p24.P24Transaction
import com.kontenery.p24.dto.P24TransactionStatus
import com.kontenery.repository.entity.ClientTable
import com.kontenery.repository.entity.PaymentTable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

private val invoiceNumbersJson = Json { ignoreUnknownKeys = true }

object P24TransactionTable : LongIdTable("p24_transaction") {
    val sessionId = varchar("session_id", 100).uniqueIndex()
    val clientId = reference("client_id", ClientTable, onDelete = ReferenceOption.CASCADE)
    val amountGrosze = integer("amount_grosze")
    val currency = varchar("currency", 3).default("PLN")
    val description = varchar("description", 255).nullable()
    val email = varchar("email", 255)
    val invoiceNumbers = text("invoice_numbers").default("[]")
    val urlReturn = varchar("url_return", 500)
    val status = enumerationByName("status", 30, P24TransactionStatus::class)
    val token = varchar("token", 255).nullable()
    val orderId = long("order_id").nullable()
    val methodId = integer("method_id").nullable()
    val statement = varchar("statement", 255).nullable()
    val paymentId = reference("payment_id", PaymentTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val errorMessage = varchar("error_message", 500).nullable()
}

class P24TransactionEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<P24TransactionEntity>(P24TransactionTable)

    var sessionId by P24TransactionTable.sessionId
    var clientId by P24TransactionTable.clientId
    var amountGrosze by P24TransactionTable.amountGrosze
    var currency by P24TransactionTable.currency
    var description by P24TransactionTable.description
    var email by P24TransactionTable.email
    var invoiceNumbers by P24TransactionTable.invoiceNumbers
    var urlReturn by P24TransactionTable.urlReturn
    var status by P24TransactionTable.status
    var token by P24TransactionTable.token
    var orderId by P24TransactionTable.orderId
    var methodId by P24TransactionTable.methodId
    var statement by P24TransactionTable.statement
    var paymentId by P24TransactionTable.paymentId
    var errorMessage by P24TransactionTable.errorMessage

    fun toDomain(): P24Transaction = P24Transaction(
        id = id.value,
        sessionId = sessionId,
        clientId = clientId.value,
        amountGrosze = amountGrosze,
        currency = currency,
        description = description,
        email = email,
        invoiceNumbers = decodeInvoiceNumbers(invoiceNumbers),
        urlReturn = urlReturn,
        status = status,
        token = token,
        orderId = orderId,
        methodId = methodId,
        statement = statement,
        paymentId = paymentId?.value,
        errorMessage = errorMessage,
    )
}

fun encodeInvoiceNumbers(numbers: List<String>): String =
    invoiceNumbersJson.encodeToString(numbers)

fun decodeInvoiceNumbers(raw: String): List<String> =
    runCatching { invoiceNumbersJson.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
