package com.kontenery.service.impl

import com.kontenery.library.model.Reading
import com.kontenery.library.model.Submeter
import com.kontenery.repository.UtilitiesRepo
import com.kontenery.service.UtilitiesService

class UtilitiesServiceImpl(utilitiesRepo: UtilitiesRepo): UtilitiesService {
    override suspend fun getSubmeters(): List<Submeter> {
        TODO("Not yet implemented")
    }

    override suspend fun getSubmeter(id: Long): Submeter? {
        TODO("Not yet implemented")
    }

    override suspend fun postSubmeter(submeter: Submeter): Submeter? {
        TODO("Not yet implemented")
    }

    override suspend fun updateSubmeter(
        id: Long,
        submeter: Submeter
    ): Submeter? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSubmeter(id: Long): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getReadings(): List<Reading> {
        TODO("Not yet implemented")
    }

    override suspend fun getReading(id: Long?): Reading? {
        TODO("Not yet implemented")
    }

    override suspend fun postReading(reading: Reading): Reading? {
        TODO("Not yet implemented")
    }

    override suspend fun updateReading(
        id: Long,
        reading: Reading
    ): Reading {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReading(id: Long): Boolean {
        TODO("Not yet implemented")
    }

}