package com.kontenery.repository.entity

import com.kontenery.model.Contract
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import java.math.BigDecimal

object ContractTable : LongIdTable("contracts") {
    val client = reference("client_id", ClientTable).nullable()
    val product = reference("product_id", ProductTable).nullable()

    val startDate = date("start_date").nullable()
    val endDate = date("end_date").nullable()

    val netPrice = decimal("net_price", precision = 12, scale = 2).nullable()
    val vatRate = decimal("vat_rate", precision = 5, scale = 2).default(BigDecimal(23))

    val needInvoice = bool("need_invoice").nullable()
}

class ContractEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ContractEntity>(ContractTable)

    var client by ClientEntity.optionalReferencedOn(ContractTable.client)
    var product by ProductEntity.optionalReferencedOn(ContractTable.product)

    var startDate by ContractTable.startDate
    var endDate by ContractTable.endDate

    var netPrice by ContractTable.netPrice
    var vatRate by ContractTable.vatRate

    var needInvoice by ContractTable.needInvoice

    fun toContract() = Contract(
        id = id.value,
        client = client?.toClient(),
        product = when(product?.type) {
            ProductType.CONTAINER -> product!!.toContainer()
            ProductType.YARD -> product!!.toYard()
            else -> null
        },
        startDate = startDate,
        endDate = endDate,
        netPrice = netPrice,
        vatRate = vatRate,
        needInvoice = needInvoice
    )
}
