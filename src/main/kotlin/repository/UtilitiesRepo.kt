package com.kontenery.repository

import com.kontenery.data.Reading
import com.kontenery.data.Submeter

interface UtilitiesRepo {
    // ---------- SUBMETER ----------
    suspend fun createSubmeter(submeter: Submeter): Submeter
    suspend fun getSubmeter(id: Long): Submeter?
    suspend fun getAllSubmeters(): List<Submeter>
    suspend fun getAllSubmetersForClient(clientId: Long): List<Submeter>
    suspend fun updateSubmeter(submeterId: Long, submeter: Submeter): Submeter?
    suspend fun deleteSubmeter(id: Long): Boolean

    // ---------- READING ----------
    suspend fun createReading(reading: Reading): Reading
    suspend fun getReading(id: Long): Reading?
    suspend fun getReadingsForSubmeter(submeterId: Long): List<Reading>
    suspend fun updateReading(readingId: Long, reading: Reading): Reading?
    suspend fun deleteReading(id: Long): Boolean
}