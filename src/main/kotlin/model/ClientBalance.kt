package com.kontenery.model

import kotlinx.datetime.LocalDate

data class ClientBalance(
    val clientId: Long,
    val from: LocalDate,
    val to: LocalDate,
    val balance: Double
)
