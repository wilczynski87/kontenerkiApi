package com.kontenery.repository.impl

import com.kontenery.data.gate.GateEventDto
import com.kontenery.repository.GateEventRepo
import com.kontenery.repository.entity.ClientEntity
import com.kontenery.repository.entity.GateEventEntity
import com.kontenery.repository.entity.GateEventTable
import com.kontenery.repository.entity.WorkerEntity
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

    override suspend fun logOpenEvent(clientId: Long, workerId: Long?, note: String?): Unit = suspendTransaction {
        val clientEntity = ClientEntity.findById(clientId)
            ?: throw IllegalArgumentException("Client not found: $clientId")
        val workerEntity = workerId?.let { id ->
            WorkerEntity.findById(id)
                ?: throw IllegalArgumentException("Worker not found: $id")
        }
        GateEventEntity.new {
            client = clientEntity
            worker = workerEntity
            openedAtEpochMs = Clock.System.now().toEpochMilliseconds()
            this.note = note
        }
    }

    override suspend fun listOpenEventsByWorkerId(workerId: Long, limit: Int): List<GateEventDto> =
        suspendTransaction {
            GateEventEntity
                .find { GateEventTable.worker eq workerId }
                .orderBy(GateEventTable.openedAtEpochMs to SortOrder.DESC)
                .limit(limit.coerceIn(1, MAX_LIST_LIMIT))
                .map { GateEventDto(openedAtEpochMs = it.openedAtEpochMs) }
        }

    companion object {
        private const val MAX_LIST_LIMIT = 500
    }
}
