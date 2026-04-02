package com.kontenery.utils

import com.kontenery.data.utils.now
import io.ktor.util.*
import kotlinx.datetime.LocalDate

fun cookRawPeriod(periodRaw: String?, placeName: String): LocalDate {
    return if(periodRaw.isNullOrBlank() || periodRaw.toLowerCasePreservingASCIIRules().trim() == "null") {
        LocalDate.now()
    } else {
        try {
            LocalDate.parse(periodRaw)
        } catch (e:Exception) {
            println("period: $e")
            throw NullPointerException("$placeName, can not parse date: $periodRaw")
        }
    }
}

fun LocalDate.startOfYear(): LocalDate =
    LocalDate(this.year, 1, 1)