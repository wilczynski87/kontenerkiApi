package com.kontenery.service.impl

import com.kontenery.data.worker.WorkerCreateRequest
import com.kontenery.data.worker.WorkerDto
import com.kontenery.repository.WorkerRepo
import com.kontenery.service.WorkerService

class WorkerServiceImpl(
    private val workerRepo: WorkerRepo,
) : WorkerService {

    override suspend fun createWorkerForClient(
        clientId: Long,
        request: WorkerCreateRequest,
    ): WorkerDto {
        val name = request.name.trim()
        val email = request.email.trim()
        val password = request.password.trim()

        require(name.isNotBlank()) { "Employee name is required" }
        require(email.isNotBlank()) { "Employee email is required" }
        require(password.length >= MIN_PASSWORD_LENGTH) {
            "Password must be at least $MIN_PASSWORD_LENGTH characters"
        }
        if (email.length > 255 || name.length > 255) {
            throw IllegalArgumentException("Employee name or email is invalid")
        }

        val normalizedEmail = email.lowercase()
        if (workerRepo.emailExistsForClient(clientId, normalizedEmail)) {
            throw IllegalArgumentException("Employee with this email already exists for the client")
        }

        return workerRepo.createWorker(clientId, name, normalizedEmail, password).toDto()
    }

    override suspend fun listWorkersForClient(clientId: Long): List<WorkerDto> =
        workerRepo.findWorkersByClientId(clientId).map { it.toDto() }

    override suspend fun deleteWorkerForClient(clientId: Long, workerId: Long): Boolean {
        val worker = workerRepo.findWorkerById(workerId) ?: return false
        if (worker.clientId != clientId) {
            return false
        }
        return workerRepo.deleteWorker(workerId)
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
