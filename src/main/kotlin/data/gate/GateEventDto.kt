package com.kontenery.data.gate

import kotlinx.serialization.Serializable

@Serializable
data class GateEventDto(
    val openedAtEpochMs: Long,
)
