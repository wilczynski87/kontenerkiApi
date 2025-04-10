package com.kontenery.model

import java.math.BigDecimal
import java.time.LocalDate

data class Contract(
    val id: Long? = null,
    val client: Client? = null,
    val product: Product? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val netPrice: BigDecimal? = null,
    val vatRate: BigDecimal = BigDecimal(23),
    val needInvoice: Boolean? = null,
)