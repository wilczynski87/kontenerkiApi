package com.kontenery.repository.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object GateEventTable : LongIdTable("gate_event") {
    val client = reference("client_id", ClientTable, onDelete = ReferenceOption.CASCADE)
    val openedAtEpochMs = long("opened_at_epoch_ms")
    val note = varchar("note", 100).nullable()
}

class GateEventEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<GateEventEntity>(GateEventTable)

    var client by ClientEntity referencedOn GateEventTable.client
    var openedAtEpochMs by GateEventTable.openedAtEpochMs
    var note by GateEventTable.note
}
