package com.kontenery.repository.impl

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.now
import com.kontenery.repository.InvoiceRepo
import com.kontenery.repository.entity.AddressDAO
import com.kontenery.repository.entity.invoice.*
import com.kontenery.repository.entity.suspendTransaction
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and

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
    ): List<Invoice> = suspendTransaction {
        val offset:Long = (page * size).toLong()
        InvoiceEntity.find {
                (InvoiceTable.customer eq clientId) and
                (InvoiceTable.invoiceDate greaterEq from) and
                (InvoiceTable.invoiceDate lessEq to)
            }
            .limit(size)
            .offset(offset)
            .map {it.toDomain()}
    }

    override suspend fun getInvoiceById(invoiceId: Long): Invoice? = suspendTransaction {
        InvoiceEntity.findById(invoiceId)?.toDomain()
    }

    override suspend fun saveInvoice(invoice: Invoice): Invoice = suspendTransaction {
        // Create seller address
        val sellerAddress = AddressDAO.new {
            street = invoice.seller?.address?.street
            house = invoice.seller?.address?.house
            city = invoice.seller?.address?.city
            postcode = invoice.seller?.address?.postCode
            country = invoice.seller?.address?.country ?: "PL"
        }

        // Create customer address
        val customerAddress = AddressDAO.new {
            street = invoice.customer?.address?.street
            house = invoice.customer?.address?.house
            city = invoice.customer?.address?.city
            postcode = invoice.customer?.address?.postCode
            country = invoice.customer?.address?.country ?: "PL"
        }
        // Create or retrieve Seller
        val sellerEntity = SubjectEntity.new {
            name = invoice.seller?.name ?: ""
            address = sellerAddress
            nip = invoice.seller?.nip ?: ""
            email = invoice.seller?.email ?: ""
            phone = invoice.seller?.phone ?: ""
            invoiceNumber = invoice.seller?.invoiceNumber ?: ""
            type = SubjectType.SELLER.name ?: ""
            account = invoice.seller?.account ?: ""
        }

        // Create or retrieve Customer
        val customerEntity = SubjectEntity.new {
            name = invoice.customer?.name ?: ""
            address = customerAddress
            nip = invoice.customer?.nip ?: ""
            email = invoice.customer?.email ?: ""
            phone = invoice.customer?.phone
            invoiceNumber = invoice.customer?.invoiceNumber
            type = SubjectType.CUSTOMER.name
            salutation = invoice.customer?.salutation
        }

        // Create Invoice
        val invoiceEntity = InvoiceEntity.new {
            invoiceNumber = invoice.invoiceNumber ?: ""
            invoiceTitle = invoice.invoiceTitle ?: ""
            invoiceDate = invoice.invoiceDate ?: LocalDate.now()
            this.seller = sellerEntity
            this.customer = customerEntity
            vatAmountSum = invoice.vatAmountSum ?: ""
            priceSum = invoice.priceSum ?: ""
            priceWithVatSum = invoice.priceWithVatSum ?: ""
            paymentDay = invoice.paymentDay ?: LocalDate.now().plus(14, DateTimeUnit.DAY)
            mainAccount = invoice.mainAccount
        }

        // Create Positions
        invoice.products.forEach { product ->
            PositionEntity.new {
                this.invoice = invoiceEntity
                productName = product.productName ?: ""
                unitPrice = product.unitPrice ?: ""
                quantity = product.quantity ?: ""
                vatRate = product.vatRate ?: "23"
                vatAmount = product.vatAmount ?: ""
                price = product.price ?: ""
                priceWithVat = product.priceWithVat ?: ""
            }
        }

        invoiceEntity.toDomain()
    }

//    override suspend fun saveInvoice(invoice: Invoice): Invoice = suspendTransaction {
//        // Create seller address
//        val sellerAddress = AddressDAO.new {
//            street = invoice.seller?.address?.street
//            house = invoice.seller?.address?.house
//            city = invoice.seller?.address?.city
//            postcode = invoice.seller?.address?.postCode
//            country = invoice.seller?.address?.country ?: "PL"
//        }
//
//        // Create customer address
//        val customerAddress = AddressDAO.new {
//            street = invoice.customer?.address?.street
//            house = invoice.customer?.address?.house
//            city = invoice.customer?.address?.city
//            postcode = invoice.customer?.address?.postCode
//            country = invoice.customer?.address?.country ?: "PL"
//        }
//        // Create or retrieve Seller
//        val sellerEntity = SubjectEntity.new {
//            name = invoice.seller?.name ?: ""
//            address = sellerAddress
//            nip = invoice.seller?.nip ?: ""
//            email = invoice.seller?.email ?: ""
//            phone = invoice.seller?.phone ?: ""
//            invoiceNumber = invoice.seller?.invoiceNumber ?: ""
//            type = SubjectType.SELLER.name ?: ""
//            account = invoice.seller?.account ?: ""
//        }
//
//        // Create or retrieve Customer
//        val customerEntity = SubjectEntity.new {
//            name = invoice.customer?.name ?: ""
//            address = customerAddress
//            nip = invoice.customer?.nip ?: ""
//            email = invoice.customer?.email ?: ""
//            phone = invoice.customer?.phone
//            invoiceNumber = invoice.customer?.invoiceNumber
//            type = SubjectType.CUSTOMER.name
//            salutation = invoice.customer?.salutation
//        }
//
//        // Create Invoice
//        val invoiceEntity = InvoiceEntity.new {
//            invoiceNumber = invoice.invoiceNumber ?: ""
//            invoiceTitle = invoice.invoiceTitle ?: ""
//            invoiceDate = invoice.invoiceDate ?: LocalDate.now()
//            this.seller = sellerEntity
//            this.customer = customerEntity
//            vatAmountSum = invoice.vatAmountSum ?: ""
//            priceSum = invoice.priceSum ?: ""
//            priceWithVatSum = invoice.priceWithVatSum ?: ""
//            paymentDay = invoice.paymentDay ?: LocalDate.now().plus(14, DateTimeUnit.DAY)
//            mainAccount = invoice.mainAccount
//        }
//
//        // Create Positions
//        invoice.products.forEach { product ->
//            PositionEntity.new {
//                this.invoice = invoiceEntity
//                productName = product.productName ?: ""
//                unitPrice = product.unitPrice ?: ""
//                quantity = product.quantity ?: ""
//                vatRate = product.vatRate
//                vatAmount = product.vatAmount ?: ""
//                price = product.price ?: ""
//                priceWithVat = product.priceWithVat ?: ""
//            }
//        }
//
//        invoiceEntity.toDomain()
//    }

    override suspend fun getLastInvoiceNumber(): String? = suspendTransaction {
        InvoiceEntity.all()
            .orderBy(InvoiceTable.invoiceDate to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.toDomain()
            ?.invoiceNumber
    }

    override suspend fun getLastBillNumber(): String? = suspendTransaction {
        BillEntity.all()
            .orderBy(BillTable.billDate to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.toDomain()
            ?.invoiceNumber
    }

    override suspend fun confirmInvoiceSendDate(invoiceNumber: String, date: LocalDate): Boolean = suspendTransaction {
        val invoice = InvoiceEntity.find {
            InvoiceTable.invoiceNumber eq invoiceNumber
        }.firstOrNull()

        invoice?.invoiceSendToClient = date

        invoice != null
    }
}