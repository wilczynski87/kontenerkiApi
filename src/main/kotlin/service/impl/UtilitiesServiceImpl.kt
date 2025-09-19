package com.kontenery.service.impl

import com.kontenery.library.model.Reading
import com.kontenery.library.model.Submeter
import com.kontenery.repository.UtilitiesRepo
import com.kontenery.service.UtilitiesService

class UtilitiesServiceImpl(private val utilitiesRepo: UtilitiesRepo): UtilitiesService {
    override suspend fun getSubmeters(): List<Submeter> {
        return utilitiesRepo.getAllSubmeters()
    }

    override suspend fun getSubmeter(id: Long): Submeter? {
        return utilitiesRepo.getSubmeter(id)
    }

    override suspend fun postSubmeter(submeter: Submeter): Submeter? {
        return utilitiesRepo.createSubmeter(submeter)
    }

    override suspend fun updateSubmeter(
        id: Long,
        submeter: Submeter
    ): Submeter? {
        return utilitiesRepo.updateSubmeter(id, submeter)
    }

    override suspend fun deleteSubmeter(id: Long): Boolean {
        return utilitiesRepo.deleteSubmeter(id)
    }


    // READINGS
    override suspend fun getReadings(submeterId: Long): List<Reading> {
        return utilitiesRepo.getReadingsForSubmeter(submeterId)
    }

    override suspend fun getReading(id: Long): Reading? {
        return utilitiesRepo.getReading(id)
    }

    override suspend fun postReading(reading: Reading): Reading? {
        return utilitiesRepo.createReading(reading)
    }

    override suspend fun updateReading(
        id: Long,
        reading: Reading
    ): Reading? {
        return utilitiesRepo.updateReading(id, reading)
    }

    override suspend fun deleteReading(id: Long): Boolean {
        return utilitiesRepo.deleteReading(id)
    }

}