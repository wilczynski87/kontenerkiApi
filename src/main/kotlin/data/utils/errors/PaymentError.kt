package com.kontenery.data.utils.errors

import com.kontenery.data.Payment
import kotlinx.serialization.Serializable

@Serializable
data class PaymentError(
    override val title: String?,
    override val message: String?,
    val payment: Payment?
) : ErrorMessage

