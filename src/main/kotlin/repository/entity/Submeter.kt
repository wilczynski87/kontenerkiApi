package com.kontenery.repository.entity

import com.kontenery.data.Reading
import com.kontenery.data.Submeter
import com.kontenery.data.utils.UtilityType
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import java.math.BigDecimal

object SubmeterTable: LongIdTable("submeter") {
    val client = reference("client", ClientTable).nullable()
    val location = varchar("location", 250)
    val number = varchar("number", 50).nullable()
    val utilityType = varchar("utility_type", 50)
    val fotoUrl = varchar("foto_url", length = 100).nullable()
}

object ReadingTable: LongIdTable("reading") {
    val submeter = reference("submeter", SubmeterTable)
    val utilityType = varchar("utility_type", 50)
    val reading = varchar("reading", 50)
    val date = date("date")
    val currentUnitPriceNet = double("current_price")
}

class SubmeterEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SubmeterEntity>(SubmeterTable)

    var client by ClientEntity optionalReferencedOn SubmeterTable.client
    var location by SubmeterTable.location
    var number by SubmeterTable.number
    var utilityType by SubmeterTable.utilityType
    var fotoUrl by SubmeterTable.fotoUrl

    // relacja 1-to-many
    val readings by ReadingEntity referrersOn ReadingTable.submeter

    fun toDomain() = Submeter(
        id = id.value,
        clientId = client?.id?.value,
        location = location,
        number = number,
        utilityType = UtilityType.valueOf(utilityType),
        fotoUrl = fotoUrl,
        readings = readings.map { it.toDomain() }
    )
}

class ReadingEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<ReadingEntity>(ReadingTable)

    var submeter by SubmeterEntity referencedOn ReadingTable.submeter
    var utilityType by ReadingTable.utilityType
    var reading by ReadingTable.reading
    var date by ReadingTable.date
    var currentUnitPriceNet by ReadingTable.currentUnitPriceNet

    fun toDomain() = Reading(
        id = id.value,
        submeterId = submeter.id.value,
        utilityType = UtilityType.valueOf(utilityType),
        reading = BigDecimal(reading),
        date = date,
        currentUnitPriceNet = BigDecimal.valueOf(currentUnitPriceNet)
    )
}