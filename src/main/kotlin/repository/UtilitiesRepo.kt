package com.kontenery.repository

import com.kontenery.library.model.Reading
import com.kontenery.library.model.Submeter

interface UtilitiesRepo {
    // ---------- SUBMETER ----------
    suspend fun createSubmeter(submeter: Submeter): Submeter
    suspend fun getSubmeter(id: Long): Submeter?
    suspend fun getAllSubmeters(): List<Submeter>
    suspend fun getAllSubmetersForClient(clientId: Long): List<Submeter>
    suspend fun updateSubmeter(id: Long, submeter: Submeter): Boolean
    suspend fun deleteSubmeter(id: Long): Boolean

    // ---------- READING ----------
    suspend fun createReading(reading: Reading): Reading
    suspend fun getReading(id: Long): Reading?
    suspend fun getReadingsForSubmeter(submeterId: Long): List<Reading>
    suspend fun updateReading(reading: Reading): Boolean
    suspend fun deleteReading(id: Long): Boolean
}