package com.kontenery.data.payment

import com.kontenery.data.PaymentDto
import com.kontenery.data.utils.errors.PaymentError
import kotlinx.serialization.Serializable

@Serializable
data class PaymentsRecogniseList(
    val newPayments: MutableList<PaymentDto>? = mutableListOf<PaymentDto>(),
    val oldPayments: MutableList<PaymentDto>? = mutableListOf(),
    val unrecognizedPayments: MutableList<PaymentDto>? = mutableListOf(),
    val errors: MutableList<PaymentError>? = mutableListOf(),
)
