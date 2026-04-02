package com.kontenery.data.utils

import kotlinx.datetime.*
import java.time.YearMonth

fun LocalDate.Companion.now(): LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

fun LocalDate.Companion.startOfCurrentMonth(period: LocalDate? = null): LocalDate {
    val period = period ?: LocalDate.now()
    return LocalDate(period.year, period.monthNumber, 1)
}

fun LocalDate.Companion.endOfCurrentMonth(period: LocalDate? = null): LocalDate {
    val period = period ?: LocalDate.now()
    return YearMonth.of(period.year, period.monthNumber).atEndOfMonth().toKotlinLocalDate()
}

fun LocalDate.Companion.startOfCurrentYear(period: LocalDate? = null): LocalDate {
    val period = period ?: LocalDate.now()
    return parse("${period.year}-01-01")
}

fun LocalDate.Companion.endOfCurrentYear(period: LocalDate? = null): LocalDate {
    val period = period ?: LocalDate.now()
    return parse("${period.year}-12-31")
}