package com.kontenery.repository

interface GateEventRepo {
    /** Epoch millis of the last open for this client, or null if never opened. */
    suspend fun getLastOpenEventEpochMs(clientId: Long): Long?

    suspend fun logOpenEvent(clientId: Long, note: String? = "yard")
}
