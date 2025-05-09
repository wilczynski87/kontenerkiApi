package com.kontenery.repository.entity.invoice

import com.kontenery.model.invoice.Position
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object Positions : LongIdTable() {
    val invoice = reference("invoice_id", InvoiceTable)
    val productName = varchar("product_name", 255)
    val unitPrice = varchar("unit_price", 30)
    val quantity = varchar("quantity", 30)  // Updated to store as a string
    val vatRate = varchar("vat_rate", 10).nullable() // Vat rate as string, default to 23 if null
    val vatAmount = varchar("vat_amount", 30)
    val price = varchar("price", 30)
    val vat = varchar("vat", 30)
    val priceWithVat = varchar("price_with_vat", 30)
}

class PositionEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<PositionEntity>(Positions)

    var invoice by InvoiceEntity referencedOn Positions.invoice
    var productName by Positions.productName
    var unitPrice by Positions.unitPrice
    var quantity by Positions.quantity
    var vatRate by Positions.vatRate
    var vatAmount by Positions.vatAmount
    var price by Positions.price
    var vat by Positions.vat
    var priceWithVat by Positions.priceWithVat

    fun toDomain(): Position = Position(
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity,
        vatRate = vatRate ?: "23",  // Default value if null
        vatAmount = vatAmount,
        price = price,
        vat = vat,
        priceWithVat = priceWithVat
    )
}