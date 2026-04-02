package com.kontenery.data


import com.kontenery.data.serializers.BigDecimalSerializer
import com.kontenery.library.utils.UtilityType
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Reading(
    val id: Long? = null,
    val submeterId: Long? = null,
    val utilityType: UtilityType? = null,
    val reading: String? = null,
    val date: LocalDate? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val currentUnitPriceNet: BigDecimal? = null
)
