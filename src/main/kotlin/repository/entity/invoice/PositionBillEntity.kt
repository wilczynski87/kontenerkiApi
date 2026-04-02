package com.kontenery.repository.entity.invoice

import com.kontenery.data.invoice.Position
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object PositionsBill : LongIdTable() {
    val bill = reference("bill_id", BillTable, onDelete = ReferenceOption.CASCADE)
    val productName = varchar("product_name", 255)
    val unitPrice = varchar("unit_price", 30)
    val quantity = varchar("quantity", 30)  // Updated to store as a string
    val price = varchar("price", 30)
}

class PositionBillEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PositionBillEntity>(PositionsBill)

    var bill by BillEntity referencedOn PositionsBill.bill
    var productName by PositionsBill.productName
    var unitPrice by PositionsBill.unitPrice
    var quantity by PositionsBill.quantity
    var price by PositionsBill.price

    fun toDomain(): Position = Position(
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity,
        vatRate = null,
        vatAmount = null,
        price = price,
        priceWithVat = null
    )
}