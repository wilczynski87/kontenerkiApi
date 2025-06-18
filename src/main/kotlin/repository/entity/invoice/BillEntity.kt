package com.kontenery.repository.entity.invoice

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.model.invoice.Subject
import com.kontenery.repository.entity.invoice.InvoiceEntity.Companion.referrersOn
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date

object BillTable: LongIdTable() {
    val billNumber = varchar("bill_number", 50)
    val billTitle = varchar("bill_title", 250)
    val billDate = date("bill_date")

    val seller = reference("seller_id", Subjects)
    val customer = reference("customer_id", Subjects)

    val priceSum = varchar("price_sum", 30)

    val paymentDay = date("payment_day")
    val mainAccount = varchar("main_account", 100)
    val billSendToClient = date("bill_send").nullable()
}

class BillEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<BillEntity>(BillTable)

    var billNumber by BillTable.billNumber
    var billTitle by BillTable.billTitle
    var billDate by BillTable.billDate

    var seller by SubjectEntity referencedOn BillTable.seller
    var customer by SubjectEntity referencedOn BillTable.customer

    var priceSum by BillTable.priceSum

    var paymentDay by BillTable.paymentDay
    var mainAccount by BillTable.mainAccount
    var billSendToClient by BillTable.billSendToClient

    val positions by PositionBillEntity referrersOn PositionsBill.bill

    fun toDomain(): Invoice = Invoice(
        invoiceNumber = billNumber,
        invoiceTitle = billTitle,
        invoiceDate = billDate,
        seller = seller.toDomain() as Subject.Seller,
        customer = customer.toDomain() as Subject.Customer,
        products = positions.map { it.toDomain() },
        vatAmountSum = null,
        priceSum = priceSum,
        priceWithVatSum = null,
        paymentDay = paymentDay,
        mainAccount = mainAccount,
        invoiceSendToClient = billSendToClient,
        vatApply = false,
    )
}