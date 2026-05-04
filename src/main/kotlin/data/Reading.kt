package com.kontenery.data


import com.kontenery.data.serializers.BigDecimalSerializer
import com.kontenery.data.serializers.LocalDateSerializer
import com.kontenery.data.utils.UtilityType
import com.kontenery.data.utils.now
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class Reading(
    val id: Long? = null,
    val submeterId: Long? = null,
    val utilityType: UtilityType? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val reading: BigDecimal? = null,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val currentUnitPriceNet: BigDecimal? = null
)

@Serializable
data class ReadingDto(
    val id: Long? = null,
    val submeterId: Long? = null,
    val utilityType: UtilityType? = null,
    val reading: String? = null,
    val date: String? = null,
    val currentUnitPriceNet: Double? = null
) {
    fun toReading(): Reading {
        return Reading(
            id = this.id,
            submeterId = this.submeterId,
            utilityType = this.utilityType,
            reading = BigDecimal(reading),
            date = if(this.date == null) LocalDate.now() else LocalDate.parse(this.date),
            currentUnitPriceNet = if(this.currentUnitPriceNet == null) null else BigDecimal(this.currentUnitPriceNet)

        )
    }
}
