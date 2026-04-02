package com.kontenery.data.utils.errors

import kotlinx.serialization.Serializable

@Serializable
sealed interface ErrorMessage {
    val title: String?
    val message: String?
}