package com.kontenery.repository.entity

import com.kontenery.library.model.Client
import com.kontenery.library.model.ClientCompanyData
import com.kontenery.library.model.ClientPersonalData
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.*

object ClientPersonalDataTable : LongIdTable("client_personal_data") {
    val firstName = text("first_name").nullable()
    val lastName = text("last_name").nullable()
    val pesel = text("pesel").nullable()
    val passport = text("passport").nullable()
    val addressId = optReference("address_id", AddressTable, onDelete = ReferenceOption.SET_NULL)
    val phone = text("phone").nullable()
    val email = text("email").nullable()
}

object ClientCompanyDataTable : LongIdTable("client_company_data") {
    val name = text("name").nullable()
    val nip = text("nip").nullable()
    val krs = text("krs").nullable()
    val addressId = optReference("address_id", AddressTable, onDelete = ReferenceOption.SET_NULL)
    val phone = text("phone").nullable()
    val email = text("email").nullable()
    val needInvoice = bool("need_invoice").nullable()
}

object ClientTable : LongIdTable("clients") {
    val personalDataId = optReference("personal_data_id", ClientPersonalDataTable, onDelete = ReferenceOption.SET_NULL)
    val companyDataId = optReference("company_data_id", ClientCompanyDataTable, onDelete = ReferenceOption.SET_NULL)
    val isActive = bool("is_active").nullable()
    val createdAt = date("created_at").nullable()
    val updatedAt = date("updated_at").nullable()
    val invoiceTitle = text("invoice_title").nullable()
}

class ClientPersonalDataEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ClientPersonalDataEntity>(ClientPersonalDataTable)

    var firstName by ClientPersonalDataTable.firstName
    var lastName by ClientPersonalDataTable.lastName
    var pesel by ClientPersonalDataTable.pesel
    var passport by ClientPersonalDataTable.passport
    var address by AddressDAO optionalReferencedOn ClientPersonalDataTable.addressId
    var phone by ClientPersonalDataTable.phone
    var email by ClientPersonalDataTable.email

    fun toClientPersonalData() = ClientPersonalData(
        id = id.value,
        firstName = firstName,
        lastName = lastName,
        pesel = pesel,
        passport = passport,
        address = address?.toAddress(),
        phone = phone,
        email = email
    )
}

class ClientCompanyDataEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ClientCompanyDataEntity>(ClientCompanyDataTable)

    var name by ClientCompanyDataTable.name
    var nip by ClientCompanyDataTable.nip
    var krs by ClientCompanyDataTable.krs
    var address by AddressDAO optionalReferencedOn ClientCompanyDataTable.addressId
    var phone by ClientCompanyDataTable.phone
    var email by ClientCompanyDataTable.email
    var needInvoice by ClientCompanyDataTable.needInvoice

    fun toClientCompanyData() = ClientCompanyData(
        id = id.value,
        name = name,
        nip = nip,
        krs = krs,
        address = address?.toAddress(),
        phone = phone,
        email = email,
        needInvoice = needInvoice
    )
}

class ClientEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ClientEntity>(ClientTable)

    var personalData by ClientPersonalDataEntity optionalReferencedOn ClientTable.personalDataId
    var companyData by ClientCompanyDataEntity optionalReferencedOn ClientTable.companyDataId
    var isActive by ClientTable.isActive
    var createdAt by ClientTable.createdAt
    var updatedAt by ClientTable.updatedAt
    var invoiceTitle by ClientTable.invoiceTitle

    fun toClient() = Client(
        id = id.value,
        clientPrivate = personalData?.toClientPersonalData(),
        clientCompany = companyData?.toClientCompanyData(),
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        invoiceTitle = invoiceTitle,
    )
}