package com.kontenery.repository.entity

import com.kontenery.model.Product
import com.kontenery.repository.entity.ContractTable.nullable
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

const val PRODUCT_TABLE_NAME: String = "products"

// Base table for Product
object ProductTable: LongIdTable(PRODUCT_TABLE_NAME) {
    val name = varchar("name", 255).nullable()
    val location = varchar("location", 255).nullable()
    val type = enumerationByName("type", 25, ProductType::class)
    val client = reference("client_id", ClientTable).nullable()

    // Container
    val length = varchar("length", 50).nullable()
    val height = varchar("height", 50).nullable()
    val color = varchar("color", 50).nullable()
    val acquireDate = date("acquire_date").nullable()
    val lastPainting = date("last_painting").nullable()
    val description = text("description").nullable()
    val photo = binary("photo_blob").nullable()

    //Yard
    val quantity = long("quantity").nullable()

}

// PRODUCT ENTITY
class ProductEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ProductEntity>(ProductTable)

    var name by ProductTable.name
    var location by ProductTable.location
    var type by ProductTable.type
    var client by ClientEntity.optionalReferencedOn(ProductTable.client)

    var length by ProductTable.length
    var height by ProductTable.height
    var color by ProductTable.color
    var acquireDate by ProductTable.acquireDate
    var lastPainting by ProductTable.lastPainting
    var description by ProductTable.description
    var photo by ProductTable.photo

    var quantity by ProductTable.quantity

//    fun toProduct() = Product(
//        id = id.value,
//        name = name,
//        location = location,
//        type = type,
//    )

    fun toContainer() = Product.Container(
        id = id.value,
        name = name,
        location = location,
        client = client?.toClient(),
        length = length,
        height = height,
        color = color,
        acquireDate = acquireDate,
        lastPainting = lastPainting,
        description = description,
        photo = photo,
    )

    fun toYard() = Product.Yard(
        id = id.value,
        name = name,
        location = location,
        client = client?.toClient(),
        quantity = quantity
    )
}

enum class ProductType {
    PRODUCT, YARD, CONTAINER
}
