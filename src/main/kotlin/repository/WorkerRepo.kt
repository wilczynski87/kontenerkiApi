package com.kontenery.repository

import com.kontenery.data.worker.Worker

interface WorkerRepo {
    suspend fun createWorker(clientId: Long, name: String, email: String, password: String): Worker

    suspend fun findWorkerById(workerId: Long): Worker?

    suspend fun findWorkersByEmail(email: String): List<Worker>

    suspend fun findWorkersByClientId(clientId: Long): List<Worker>

    suspend fun findClientIdByWorkerId(workerId: Long): Long?

    suspend fun emailExistsForClient(clientId: Long, email: String): Boolean

    suspend fun deleteWorker(workerId: Long): Boolean
}
