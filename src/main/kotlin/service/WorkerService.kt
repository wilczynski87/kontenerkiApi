package com.kontenery.service

import com.kontenery.data.worker.WorkerCreateRequest
import com.kontenery.data.worker.WorkerDto

interface WorkerService {
    suspend fun createWorkerForClient(clientId: Long, request: WorkerCreateRequest): WorkerDto

    suspend fun listWorkersForClient(clientId: Long): List<WorkerDto>

    suspend fun deleteWorkerForClient(clientId: Long, workerId: Long): Boolean
}
