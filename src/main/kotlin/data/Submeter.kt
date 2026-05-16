package com.kontenery.data

import com.kontenery.data.utils.UtilityType
import kotlinx.serialization.Serializable
import kotlin.Long

@Serializable
data class Submeter(
    val id: Long? = null,
    val clientId: Long? = null,
    val location: String? = null,
    val utilityType: UtilityType? = null,
    val readings: List<Reading> = emptyList(),
    val number: String? = null,
    val fotoUrl: String? = null,
)

@Serializable
data class SubmeterDto(
    val id: Long? = null,
    val clientId: Long? = null,
    val location: String? = null,
    val utilityType: UtilityType? = null,
    val readings: List<ReadingDto> = emptyList(),
    val number: String? = null,
    val fotoUrl: String? = null,
){
    fun toSubmeter() = Submeter(
            id = id,
            clientId = clientId,
            location = location,
            utilityType = utilityType,
            readings = readings.map { dto -> dto.toReading() },
            number = number,
            fotoUrl = fotoUrl,
        )
}