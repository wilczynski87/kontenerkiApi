package com.kontenery.repository.impl

import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.now
import com.kontenery.repository.BillRepo
import com.kontenery.repository.entity.AddressDAO
import com.kontenery.repository.entity.invoice.*
import com.kontenery.repository.entity.suspendTransaction
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class BillRepoImpl: BillRepo {
    override suspend fun saveBill(bill: Invoice): Invoice  = suspendTransaction {
        // Create seller address
        val sellerAddress = AddressDAO.new {
            street = bill.seller?.address?.street
            house = bill.seller?.address?.house
            city = bill.seller?.address?.city
            postcode = bill.seller?.address?.postCode
            country = bill.seller?.address?.country ?: "PL"
        }
//        println("sellerAddress: $sellerAddress")

        // Create customer address
        val customerAddress = AddressDAO.new {
            street = bill.customer?.address?.street
            house = bill.customer?.address?.house
            city = bill.customer?.address?.city
            postcode = bill.customer?.address?.postCode
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
            invoiceNumber = bill.customer?.invoiceNumber
            type = SubjectType.CUSTOMER.name
            salutation = bill.customer?.salutation
        }
//        println("customerEntity: $customerEntity")

        // Create Invoice
        val billEntity = BillEntity.new {
            billNumber = bill.invoiceNumber ?: ""
            billTitle = bill.invoiceTitle ?: ""
            billDate = bill.invoiceDate ?: LocalDate.now()
            this.seller = sellerEntity
            this.customer = customerEntity
            priceSum = bill.priceSum ?: ""
            paymentDay = bill.paymentDay ?: LocalDate.now().plus(14, DateTimeUnit.DAY)
            mainAccount = bill.mainAccount
        }
//        println("billEntity: $billEntity")

        // Create Positions
        bill.products.forEach { product ->
            PositionBillEntity.new {
                this.bill = billEntity
                productName = product.productName ?: ""
                unitPrice = product.unitPrice ?: ""
                quantity = product.quantity ?: ""
                price = product.price ?: ""
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
    ): List<Invoice> {
        TODO("Not yet implemented")
    }

    override suspend fun getBillById(invoiceId: Long): Invoice? {
        TODO("Not yet implemented")
    }

    override suspend fun getLastBillNumber(): String? {
        TODO("Not yet implemented")
    }

    override suspend fun confirmBillSendDate(invoiceNumber: String, date: LocalDate): Boolean {
        TODO("Not yet implemented")
    }
}