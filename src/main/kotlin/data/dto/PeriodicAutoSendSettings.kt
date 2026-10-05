package com.kontenery.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PeriodicAutoSendSettings(
    val enabled: Boolean,
)

@Serializable
data class PeriodicAutoSendSkipResponse(
    val skipped: Boolean,
    val reason: String,
)
