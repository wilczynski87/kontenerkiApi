package com.kontenery.repository.entity.invoice

import com.kontenery.data.Address
import com.kontenery.data.invoice.Subject
import com.kontenery.repository.entity.*
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object Subjects : LongIdTable() {
    val client = optReference("client", ClientTable)
    val name = varchar("name", 255)
    val address = optReference("address_id", AddressTable)
//    reference("address_id", AddressTable)
    val nip = varchar("nip", 20).nullable()
    val email = varchar("email", 255)
    val phone = varchar("phone", 30).nullable()

    val type = varchar("type", 20) // "customer" or "seller"

    val salutation = varchar("salutation", 100).nullable()
    val account = varchar("account", 100).nullable()

    val invoiceNumber = varchar("invoice_number", 12).nullable()
}

class SubjectEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SubjectEntity>(Subjects)

    var client by ClientEntity optionalReferencedOn Subjects.client
    var name by Subjects.name
    var address by AddressEntity optionalReferencedOn Subjects.address
    var nip by Subjects.nip
    var email by Subjects.email
    var phone by Subjects.phone
    var type by Subjects.type

    var salutation by Subjects.salutation
    var account by Subjects.account
    var invoiceNumber by Subjects.invoiceNumber

    fun toDomain(): Subject = when (type.lowercase()) {
        SubjectType.CUSTOMER.dbValue -> Subject.Customer(
            name = name,
            address = address?.toAddress(),
            nip = nip,
            email = email,
            phone = phone,
            invoiceNumber = invoiceNumber,
            salutation = salutation ?: "Drogi Kliencie",
            client = client?.toClient(),
        )
        SubjectType.SELLER.dbValue -> Subject.Seller(
            name = name,
            address = address?.toAddress() ?: Address(),
            nip = nip ?: "",
            email = email,
            phone = phone,
            invoiceNumber = invoiceNumber,
            account = account ?: ""
        )
        else -> error("Unknown Subject type: $type")
    }

    fun fromDomain(subject: Subject) {
        name = subject.name
        nip = subject.nip
        email = subject.email
        phone = subject.phone
        invoiceNumber = subject.invoiceNumber
        when (subject) {
            is Subject.Customer -> {
                type = SubjectType.CUSTOMER.dbValue
                salutation = subject.salutation
                account = null
            }
            is Subject.Seller -> {
                type = SubjectType.SELLER.dbValue
                salutation = null
                account = subject.account
            }
        }
    }
}

enum class SubjectType(val dbValue: String) {
    SELLER("seller"),
    CUSTOMER("customer"),
}
