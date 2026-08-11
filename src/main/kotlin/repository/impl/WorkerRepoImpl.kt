package com.kontenery.repository.impl

import com.kontenery.data.worker.Worker
import com.kontenery.repository.WorkerRepo
import com.kontenery.repository.entity.ClientEntity
import com.kontenery.repository.entity.WorkerEntity
import com.kontenery.repository.entity.WorkerTable
import com.kontenery.repository.entity.suspendTransaction
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

class WorkerRepoImpl : WorkerRepo {

    override suspend fun createWorker(
        clientId: Long,
        name: String,
        email: String,
        password: String,
    ): Worker = suspendTransaction {
        val clientEntity = ClientEntity.findById(clientId)
            ?: throw IllegalArgumentException("Client not found: $clientId")

        val normalizedEmail = email.trim().lowercase()
        val createdAtDate = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        WorkerEntity.new {
            client = clientEntity
            this.name = name.trim()
            this.email = normalizedEmail
            this.password = password
            createdAt = createdAtDate
        }.toWorker()
    }

    override suspend fun findWorkerById(workerId: Long): Worker? = suspendTransaction {
        WorkerEntity.findById(workerId)?.toWorker()
    }

    override suspend fun findWorkersByEmail(email: String): List<Worker> = suspendTransaction {
        val normalizedEmail = email.trim().lowercase()
        WorkerEntity.find { WorkerTable.email eq normalizedEmail }
            .map { it.toWorker() }
    }

    override suspend fun findWorkersByClientId(clientId: Long): List<Worker> = suspendTransaction {
        WorkerEntity.find { WorkerTable.client eq clientId }
            .map { it.toWorker() }
    }

    override suspend fun findClientIdByWorkerId(workerId: Long): Long? = suspendTransaction {
        WorkerEntity.findById(workerId)?.client?.id?.value
    }

    override suspend fun emailExistsForClient(clientId: Long, email: String): Boolean = suspendTransaction {
        val normalizedEmail = email.trim().lowercase()
        WorkerEntity.find {
            (WorkerTable.client eq clientId) and (WorkerTable.email eq normalizedEmail)
        }.limit(1).firstOrNull() != null
    }

    override suspend fun deleteWorker(workerId: Long): Boolean = suspendTransaction {
        WorkerEntity.findById(workerId)?.delete() != null
    }
}
