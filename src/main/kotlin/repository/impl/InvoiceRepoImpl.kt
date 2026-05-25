package com.kontenery.repository.impl

import com.kontenery.data.invoice.Invoice
import com.kontenery.data.utils.now
import com.kontenery.data.utils.startOfCurrentMonth
import com.kontenery.model.invoice.InvoiceNumber
import com.kontenery.repository.InvoiceRepo
import com.kontenery.repository.entity.AddressEntity
import com.kontenery.repository.entity.ClientEntity
import com.kontenery.repository.entity.invoice.*
import com.kontenery.repository.entity.suspendTransaction
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.text.NumberFormat
import java.util.Locale

class InvoiceRepoImpl(): InvoiceRepo {

    override suspend fun getInvoicesForDate(page: Int, size: Int, from: LocalDate, to: LocalDate): List<Invoice> = suspendTransaction {
        val offset:Long = (page * size).toLong()
        InvoiceEntity.find {
                (InvoiceTable.invoiceDate greaterEq from) and
                (InvoiceTable.invoiceDate lessEq to)
            }
            .limit(size)
            .offset(offset)
            .map {it.toDomain()}
    }

    override suspend fun getInvoicesForClient(
        page: Int,
        size: Int,
        clientId: Long,
        from: LocalDate,
        to: LocalDate
    ): List<Invoice> = newSuspendedTransaction {
        val offset:Long = (page * size).toLong()

        val customersIds = SubjectEntity
            .find { Subjects.client eq clientId }
            .map { it.id }

        InvoiceEntity.find {
                (InvoiceTable.customer inList customersIds) and
                (InvoiceTable.invoiceDate.between(from, to))
            }
            .limit(size)
            .offset(offset)
            .map {it.toDomain()}
    }

    override suspend fun getInvoiceByNumber(invoiceNumber: String): Invoice? = newSuspendedTransaction {
        InvoiceEntity.find { InvoiceTable.invoiceNumber eq invoiceNumber }
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun saveInvoice(invoice: Invoice): Invoice = suspendTransaction {
        println("saveInvoice start, number: ${invoice.invoiceNumber}")

        val sellerAddress = AddressEntity.new {
            street = invoice.seller?.address?.street
            house = invoice.seller?.address?.house
            city = invoice.seller?.address?.city
            postCode = invoice.seller?.address?.postCode
            country = invoice.seller?.address?.country ?: "PL"
        }

        val customerAddress = AddressEntity.new {
            street = invoice.customer?.address?.street
            house = invoice.customer?.address?.house
            city = invoice.customer?.address?.city
            postCode = invoice.customer?.address?.postCode
            country = invoice.customer?.address?.country ?: "PL"
        }

        val sellerEntity = SubjectEntity.new {
            name = invoice.seller?.name ?: ""
            address = sellerAddress
            nip = invoice.seller?.nip ?: ""
            email = invoice.seller?.email ?: ""
            phone = invoice.seller?.phone ?: ""
            invoiceNumber = invoice.seller?.invoiceNumber ?: ""
            type = SubjectType.SELLER.dbValue
            account = invoice.seller?.account ?: ""
        }

        val customerEntity = SubjectEntity.new {
            name = invoice.customer?.name ?: ""
            address = customerAddress
            nip = invoice.customer?.nip ?: ""
            email = invoice.customer?.email ?: ""
            phone = invoice.customer?.phone
            invoiceNumber = invoice.invoiceNumber
                ?: throw IllegalStateException("Can nato save invoice with customer - missing InvoiceNumber")
            type = SubjectType.CUSTOMER.dbValue
            salutation = invoice.customer?.salutation
            client = invoice.customer?.client?.id?.let { ClientEntity.findById(it) }
        }

        val invoiceEntity = InvoiceEntity.new {
            invoiceNumber = invoice.invoiceNumber
                ?: throw IllegalStateException("Can not save invoice - missing InvoiceNumber")
            invoiceTitle = invoice.invoiceTitle ?: ""
            invoiceDate = invoice.invoiceDate ?: LocalDate.now()
            this.seller = sellerEntity
            this.customer = customerEntity
            vatAmountSum = invoice.vatAmountSum?.roundSum() ?: ""
            priceSum = invoice.priceSum?.roundSum() ?: ""
            priceWithVatSum = invoice.priceWithVatSum?.roundSum() ?: ""
            paymentDay = invoice.paymentDay ?: LocalDate.now().plus(14, DateTimeUnit.DAY)
            mainAccount = invoice.mainAccount
            invoiceType = invoice.type
            ksefNumber = invoice.ksefNumber
        }

        invoice.products.forEach { product ->
            PositionEntity.new {
                this.invoice = invoiceEntity
                productName = product.productName ?: ""
                unitPrice = product.unitPrice ?: ""
                quantity = product.quantity ?: ""
                vatRate = product.vatRate ?: "23"
                vatAmount = product.vatAmount?.roundSum() ?: ""
                price = product.price?.roundSum() ?: ""
                priceWithVat = product.priceWithVat?.roundSum() ?: ""
            }
        }

        invoiceEntity.toDomain()
    }


    override suspend fun getLastInvoiceNumber(): String? = suspendTransaction {
        val currentMonth: LocalDate = LocalDate.startOfCurrentMonth()
        InvoiceEntity.find {
                InvoiceTable.invoiceDate greaterEq currentMonth
            }
            .map { InvoiceNumber.toInvoiceNumber(it.invoiceNumber) }
            .maxByOrNull { it.number }
            ?.toInvoiceNumberString()
    }

    override suspend fun getLastBillNumber(): String? = suspendTransaction {
        BillEntity.find {
                BillTable.billDate greaterEq LocalDate.startOfCurrentMonth()
            }
            .map { InvoiceNumber.toInvoiceNumber(it.billNumber) }
            .maxByOrNull { it.number }
            ?.toInvoiceNumberString()
    }

    override suspend fun getLastInvoiceForClient(clientId: Long): Invoice? = suspendTransaction {

        val customersIds = SubjectEntity
            .find { Subjects.client eq clientId }
            .map { it.id }

        InvoiceEntity.find { InvoiceTable.customer inList customersIds }
            .orderBy(InvoiceTable.invoiceDate to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun confirmInvoiceSendDate(invoiceNumber: String, date: LocalDate): Boolean = newSuspendedTransaction {
        val isInvoice = !invoiceNumber.endsWith('r')
        try {
            if(isInvoice) {
                InvoiceEntity.find {
                    InvoiceTable.invoiceNumber eq invoiceNumber
                }.firstOrNull()?.apply {
                    invoiceSendToClient = date
                } != null
            } else {
                BillEntity.find { BillTable.billNumber eq invoiceNumber }.firstOrNull()?.apply {
                    billSendToClient = date
                } != null
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("confirmInvoiceSendDate: No invoice found for: $invoiceNumber, ${e.message}")
        }
    }

    private fun String.roundSum(decimals: Int = 2): String {
        return this
            .replace(',', '.')
            .toBigDecimal()                  // konwersja String → BigDecimal
            .setScale(decimals, java.math.RoundingMode.HALF_UP) // zaokrąglenie
            .toPlainString()                  // z powrotem na String
    }

    private fun String.toDoublePl(): Double =
        NumberFormat.getInstance(Locale("pl", "PL"))
            .parse(this)?.toDouble()
            ?: error("Nieprawidłowa liczba: $this")
}