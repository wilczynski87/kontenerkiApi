package com.kontenery.service

import com.kontenery.data.gate.OpenGateResponse

interface GateService {
    /** Sprawdza JWT userId i zwraca clientId. */
    fun checkUserAuthenticated(userId: String?): Long

    /** Wymaga co najmniej jednej aktywnej umowy. */
    suspend fun ensureActiveContract(clientId: Long)

    /** Odmawia dostępu przy ujemnym saldzie (zaległości). */
    suspend fun ensureNoOverdue(clientId: Long)

    /** Cooldown między kolejnymi otwarciami. */
    suspend fun ensureCooldown(clientId: Long)

    /** Wywołanie hardware / mock. */
    suspend fun openGate(): OpenGateResponse

    /** Audyt otwarcia. */
    suspend fun logOpenEvent(clientId: Long)
}
