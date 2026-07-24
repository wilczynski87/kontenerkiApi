package com.kontenery.repository.impl

import com.kontenery.repository.GateEventRepo
import com.kontenery.repository.entity.ClientEntity
import com.kontenery.repository.entity.GateEventEntity
import com.kontenery.repository.entity.GateEventTable
import com.kontenery.repository.entity.suspendTransaction
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder

class GateEventRepoImpl : GateEventRepo {
    override suspend fun getLastOpenEventEpochMs(clientId: Long): Long? = suspendTransaction {
        GateEventEntity
            .find { GateEventTable.client eq clientId }
            .orderBy(GateEventTable.openedAtEpochMs to SortOrder.DESC)
            .limit(1)
            .firstOrNull()
            ?.openedAtEpochMs
    }

    override suspend fun logOpenEvent(clientId: Long, note: String?): Unit = suspendTransaction {
        val clientEntity = ClientEntity.findById(clientId)
            ?: throw IllegalArgumentException("Client not found: $clientId")
        GateEventEntity.new {
            client = clientEntity
            openedAtEpochMs = Clock.System.now().toEpochMilliseconds()
            this.note = note
        }
    }
}
