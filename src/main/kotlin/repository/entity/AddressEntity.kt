package com.kontenery.repository.entity

import com.kontenery.data.Address
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object AddressTable: LongIdTable("address") {
    val street = varchar("street", 255).nullable() // Nullable as per the class definition
    val house = varchar("house", 50).nullable() // Nullable
    val city = varchar("city", 255).nullable() // Nullable
    var postCode = varchar("post_code", 20).nullable() // Nullable
    val country = varchar("country", 3).default("PL") // Default value for country is "PL"
}

class AddressEntity(id: EntityID<Long>): LongEntity(id) {
    companion object: EntityClass<Long, AddressEntity>(AddressTable)

    var street by AddressTable.street
    var house by AddressTable.house
    var city by AddressTable.city
    var postCode by AddressTable.postCode
    var country by AddressTable.country

}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(Dispatchers.IO, statement = block)

fun addressDAOToAddress(dao: AddressEntity) = Address (
    id = dao.id.value,
    street = dao.street,
    house = dao.house,
    city = dao.city,
    country = dao.country,
    postCode = dao.postCode,
)

fun AddressEntity.toAddress(): Address {
    return Address (
        id = this.id.value,
        street = this.street,
        house = this.house,
        city = this.city,
        country = this.country,
        postCode = this.postCode,
    )
}