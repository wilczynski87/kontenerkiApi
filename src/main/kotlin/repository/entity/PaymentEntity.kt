package com.kontenery.repository.entity

import com.kontenery.library.model.Payment
import com.kontenery.library.utils.SellerAccount
import com.kontenery.repository.entity.invoice.InvoiceEntity
import com.kontenery.repository.entity.invoice.InvoiceTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.date

object PaymentTable: LongIdTable("payments") {
    val amount = decimal("amount", 15, 2) // assuming standard precision
    val date = date("date")
    val fromClient = reference("from_client_id", ClientTable) // assuming Clients table exists
    val method = varchar("method", 100).nullable()
    val toAccount = enumerationByName("to_account", 50, SellerAccount::class).nullable()
    val fromAccount = varchar("from_account", 100).nullable()
    val title = varchar("title", 255).nullable()
    val referenceNumber = varchar("reference_number", 100).nullable()
}

class PaymentEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PaymentEntity>(PaymentTable)

    var amount by PaymentTable.amount
    var date by PaymentTable.date
    var fromClient by ClientEntity referencedOn PaymentTable.fromClient
    var method by PaymentTable.method
    var toAccount by PaymentTable.toAccount
    var fromAccount by PaymentTable.fromAccount
    var title by PaymentTable.title
    var referenceNumber by PaymentTable.referenceNumber

    // Assuming many-to-many relationship with Invoice
    var forInvoices by InvoiceEntity via PaymentInvoices

    fun toDomain() = Payment(
        id = id.value,
        amount = amount,
        date = date,
        fromClient = fromClient.toClient(),
        method = method,
        toAccount = toAccount,
        fromAccount = fromAccount,
        title = title,
        forInvoices = forInvoices.map { it.toDomain() },
        referenceNumber = referenceNumber,
    )

}

object PaymentInvoices : Table() {
    val payment = reference("payment_id", PaymentTable)
    val invoice = reference("invoice_id", InvoiceTable)

    override val primaryKey = PrimaryKey(payment, invoice)
}