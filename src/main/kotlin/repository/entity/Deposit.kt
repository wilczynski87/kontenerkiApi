package com.kontenery.repository.entity

import com.kontenery.data.Deposit
import com.kontenery.data.DepositType
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object DepositTable: LongIdTable("deposit") {
    val depositType = varchar("deposit_type", 50)
    val amount = decimal("amount", 12, 2).nullable()
    val note = varchar("note", 150).nullable()
}

class DepositEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DepositEntity>(DepositTable)

    var depositType by DepositTable.depositType
    var amount by DepositTable.amount
    var note by DepositTable.note

    fun toDomain() = Deposit(
        type = DepositType.valueOf(depositType),
        amount = amount.toString(),
        note = note
    )
}