package com.kontenery.data.utils.errors

import com.kontenery.data.serializers.LocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class InvoiceErrorMessage(
    override val title: String? = null,
    override val message: String? = null,
    val invoiceId: Long? = null,
    val clientId: Long? = null,
    @Serializable(with = LocalDateSerializer::class)
    val period: LocalDate? = null,
): ErrorMessage
