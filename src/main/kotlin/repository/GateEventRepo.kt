package com.kontenery.repository

import com.kontenery.data.gate.GateEventDto

interface GateEventRepo {
    /** Epoch millis of the last open for this client, or null if never opened. */
    suspend fun getLastOpenEventEpochMs(clientId: Long): Long?

    suspend fun logOpenEvent(clientId: Long, workerId: Long? = null, note: String? = "yard")

    suspend fun listOpenEventsByWorkerId(workerId: Long, limit: Int = 100): List<GateEventDto>
}
