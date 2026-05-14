package com.kontenery.data.utils.errors

import kotlinx.serialization.Serializable

@Serializable
data class BankAccountError(
    override val title: String?,
    override val message: String?,
    val bankAccount: String? = null,
    val existingId: Long? = null,
) : ErrorMessage
