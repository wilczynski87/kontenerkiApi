package com.kontenery.repository.entity.invoice

import com.kontenery.data.invoice.Invoice
import com.kontenery.data.invoice.Subject
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.date

object InvoiceTable: LongIdTable() {
    val invoiceNumber = varchar("invoice_number", 50)
    val invoiceTitle = varchar("invoice_title", 250)
    val invoiceDate = date("invoice_date")

    val seller = reference("seller_id", Subjects, onDelete = ReferenceOption.CASCADE)
    val customer = reference("customer_id", Subjects, onDelete = ReferenceOption.CASCADE)

    val vatAmountSum = varchar("vat_amount_sum", 30)
    val priceSum = varchar("price_sum", 30)
    val priceWithVatSum = varchar("price_with_vat_sum", 30)

    val paymentDay = date("payment_day")
    val mainAccount = varchar("main_account", 100)
    val invoiceSendToClient = date("invoice_send").nullable()

    val invoiceType = varchar("invoice_type", 50).nullable()
}

class InvoiceEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<InvoiceEntity>(InvoiceTable)

    var invoiceNumber by InvoiceTable.invoiceNumber
    var invoiceTitle by InvoiceTable.invoiceTitle
    var invoiceDate by InvoiceTable.invoiceDate

    var seller by SubjectEntity referencedOn InvoiceTable.seller
    var customer by SubjectEntity referencedOn InvoiceTable.customer

    var vatAmountSum by InvoiceTable.vatAmountSum
    var priceSum by InvoiceTable.priceSum
    var priceWithVatSum by InvoiceTable.priceWithVatSum

    var paymentDay by InvoiceTable.paymentDay
    var mainAccount by InvoiceTable.mainAccount
    var invoiceSendToClient by InvoiceTable.invoiceSendToClient

    var invoiceType by InvoiceTable.invoiceType

    val positions by PositionEntity referrersOn Positions.invoice

    fun toDomain(): Invoice = Invoice(
        invoiceNumber = invoiceNumber,
        invoiceTitle = invoiceTitle,
        invoiceDate = invoiceDate,
        seller = seller.toDomain() as Subject.Seller,
        customer = customer.toDomain() as Subject.Customer,
        products = positions.map { it.toDomain() },
        vatAmountSum = vatAmountSum,
        priceSum = priceSum,
        priceWithVatSum = priceWithVatSum,
        paymentDay = paymentDay,
        mainAccount = mainAccount,
        invoiceSendToClient = invoiceSendToClient,
        type = invoiceType,
        vatApply = true,
    )
}
