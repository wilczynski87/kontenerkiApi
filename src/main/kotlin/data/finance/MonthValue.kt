package com.kontenery.library.model.finance

import kotlinx.serialization.Serializable

@Serializable
data class MonthValue(
    val month: String, // "01.2026"
    val label: String  // "styczeń"
)