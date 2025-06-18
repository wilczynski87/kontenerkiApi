package com.kontenery.repository.entity.invoice

import com.kontenery.library.model.invoice.Subject
import com.kontenery.repository.entity.AddressDAO
import com.kontenery.repository.entity.AddressTable
import com.kontenery.repository.entity.toAddress
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object Subjects : LongIdTable() {
    val name = varchar("name", 255)
    val address = reference("address_id", AddressTable)
    val nip = varchar("nip", 20)
    val email = varchar("email", 255)
    val phone = varchar("phone", 30).nullable()

    val type = varchar("type", 20) // "customer" or "seller"

    val salutation = varchar("salutation", 100).nullable()
    val account = varchar("account", 100).nullable()

    val invoiceNumber = varchar("invoice_number", 12).nullable()
}

class SubjectEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SubjectEntity>(Subjects)

    var name by Subjects.name
    var address by AddressDAO referencedOn Subjects.address
    var nip by Subjects.nip
    var email by Subjects.email
    var phone by Subjects.phone
    var type by Subjects.type

    var salutation by Subjects.salutation
    var account by Subjects.account
    var invoiceNumber by Subjects.invoiceNumber

    fun toDomain(): Subject = when (type) {
        SubjectType.CUSTOMER.name -> Subject.Customer(
            name = name,
            address = address.toAddress(),
            nip = nip,
            email = email,
            phone = phone,
            invoiceNumber = invoiceNumber,
            salutation = salutation ?: "Drogi Kliencie"
        )
        SubjectType.SELLER.name -> Subject.Seller(
            name = name,
            address = address.toAddress(),
            nip = nip,
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
                type = "customer"
                salutation = subject.salutation
                account = null
            }
            is Subject.Seller -> {
                type = "seller"
                salutation = null
                account = subject.account
            }
        }
    }
}

enum class SubjectType {
    SELLER, CUSTOMER
}
