package com.kontenery.data.gate

import kotlinx.serialization.Serializable

@Serializable
data class OpenGateRequest(
    val reservationId: String? = null,
)

@Serializable
data class OpenGateResponse(
    val success: Boolean = true,
    val message: String = "",
    val reservationId: String? = null,
)
