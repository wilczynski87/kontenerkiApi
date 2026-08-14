package com.kontenery.service

import com.kontenery.data.gate.OpenGateResponse

interface GateService {
    /** Sprawdza JWT userId i zwraca clientId. */
    suspend fun checkUserAuthenticated(userId: String?, role: String?): Long

    /** Wymaga co najmniej jednej aktywnej umowy. */
    suspend fun ensureActiveContract(clientId: Long)

    /** Odmawia dostępu przy ujemnym saldzie (zaległości). Pomijane dla admina (userId 0). */
    suspend fun ensureNoOverdue(clientId: Long)

    /** Cooldown między kolejnymi otwarciami. Pomijane dla admina (userId 0). */
    suspend fun ensureCooldown(clientId: Long)

    /** Wywołanie hardware / mock. */
    suspend fun openGate(): OpenGateResponse

    /** Audyt otwarcia (opcjonalnie przypisany do pracownika). Pomijane dla admina (userId 0). */
    suspend fun logOpenEvent(clientId: Long, workerId: Long? = null)
}
