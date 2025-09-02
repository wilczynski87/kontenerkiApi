package com.kontenery.repository.impl

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.now
import com.kontenery.repository.BillRepo
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

class BillRepoImpl: BillRepo {
    override suspend fun saveBill(bill: Invoice): Invoice = suspendTransaction {
        // Create seller address
        val sellerAddress = AddressEntity.new {
            street = bill.seller?.address?.street
            house = bill.seller?.address?.house
            city = bill.seller?.address?.city
            postCode = bill.seller?.address?.postCode
            country = bill.seller?.address?.country ?: "PL"
        }
//        println("sellerAddress: $sellerAddress")

        // Create customer address
        val customerAddress = AddressEntity.new {
            street = bill.customer?.address?.street
            house = bill.customer?.address?.house
            city = bill.customer?.address?.city
            postCode = bill.customer?.address?.postCode
            country = bill.customer?.address?.country ?: "PL"
        }
//        println("customerAddress: $customerAddress")

        // Create or retrieve Seller
        val sellerEntity = SubjectEntity.new {
            name = bill.seller?.name ?: ""
            address = sellerAddress
            nip = bill.seller?.nip ?: ""
            email = bill.seller?.email ?: ""
            phone = bill.seller?.phone ?: ""
            invoiceNumber = bill.seller?.invoiceNumber ?: ""
            type = SubjectType.SELLER.name ?: ""
            account = bill.seller?.account ?: ""
        }
//        println("sellerEntity: $sellerEntity")

        // Create or retrieve Customer
        val customerEntity = SubjectEntity.new {
            name = bill.customer?.name ?: ""
            address = customerAddress
            nip = bill.customer?.nip ?: ""
            email = bill.customer?.email ?: ""
            phone = bill.customer?.phone
            invoiceNumber = bill.invoiceNumber ?: throw IllegalStateException("Can nato save bill with Customer - missing BillNumber")
            type = SubjectType.CUSTOMER.name
            salutation = bill.customer?.salutation
            client = bill.customer?.client?.id?.let { ClientEntity.findById(it) }
        }
//        println("customerEntity: $customerEntity")

        // Create Invoice
        val billEntity = BillEntity.new {
            billNumber = bill.invoiceNumber ?: throw IllegalStateException("Can nato save bill - missing BillNumber")
            billTitle = bill.invoiceTitle ?: ""
            billDate = bill.invoiceDate ?: LocalDate.now()
            this.seller = sellerEntity
            this.customer = customerEntity
            priceSum = bill.priceSum?.roundSum() ?: ""
            paymentDay = bill.paymentDay ?: LocalDate.now().plus(14, DateTimeUnit.DAY)
            mainAccount = bill.mainAccount
            billType = bill.type
        }
//        println("billEntity: $billEntity")

        // Create Positions
        bill.products.forEach { product ->
            PositionBillEntity.new {
                this.bill = billEntity
                productName = product.productName ?: ""
                unitPrice = product.unitPrice ?: ""
                quantity = product.quantity ?: ""
                price = product.price?.roundSum() ?: ""
            }
        }
//        println("products: ${bill.products}")

        billEntity.toDomain()
    }

    override suspend fun getBillForDate(page: Int, size: Int, from: LocalDate, to: LocalDate): List<Invoice> {
        TODO("Not yet implemented")
    }

    override suspend fun getBillsForClient(
        page: Int,
        size: Int,
        clientId: Long,
        from: LocalDate,
        to: LocalDate
    ): List<Invoice> = suspendTransaction {
        val offset:Long = (page * size).toLong()

        val customersIds = SubjectEntity
            .find { Subjects.client eq clientId }
            .map { it.id }

        BillEntity.find {
                (BillTable.customer inList customersIds) and
                (BillTable.billDate greaterEq from) and
                (BillTable.billDate lessEq to)
            }
            .limit(size)
            .offset(offset)
            .map {it.toDomain()}
    }

    override suspend fun getBillById(invoiceId: Long): Invoice? = suspendTransaction {
        TODO("Not yet implemented")
    }

    override suspend fun getBillByNumber(billNumber: String): Invoice? = newSuspendedTransaction {
        BillEntity.find { BillTable.billNumber eq billNumber }
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun getLastBillNumber(): String? = suspendTransaction {
        TODO("Not yet implemented")
    }

    override suspend fun getLastBillForClient(clientId: Long): Invoice? = suspendTransaction {
        val customersIds = SubjectEntity
            .find { Subjects.client eq clientId }
            .map { it.id }

        BillEntity.find { BillTable.customer inList customersIds }
            .orderBy(BillTable.billDate to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun confirmBillSendDate(invoiceNumber: String, date: LocalDate): Boolean = suspendTransaction {
        TODO("Not yet implemented")
    }

    private fun String.roundSum(decimals: Int = 2): String {
        return this
            .toBigDecimal()                  // konwersja String → BigDecimal
            .setScale(decimals, java.math.RoundingMode.HALF_UP) // zaokrąglenie
            .toPlainString()                  // z powrotem na String
    }
}