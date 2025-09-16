package com.kontenery.service

import com.kontenery.library.model.Reading
import com.kontenery.library.model.Submeter

interface UtilitiesService {

    suspend fun getSubmeters(): List<Submeter>
    suspend fun getSubmeter(id: Long): Submeter?
    suspend fun postSubmeter(submeter: Submeter): Submeter?
    suspend fun updateSubmeter(id: Long, submeter: Submeter): Submeter?
    suspend fun deleteSubmeter(id: Long): Boolean

    suspend fun getReadings(): List<Reading>
    suspend fun getReading(id:Long?): Reading?
    suspend fun postReading(reading:Reading): Reading?
    suspend fun updateReading(id: Long, reading: Reading): Reading?
    suspend fun deleteReading(id: Long): Boolean

}