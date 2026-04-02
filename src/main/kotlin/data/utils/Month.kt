package com.kontenery.data.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

enum class Month(val number: Int, val polishName: String) {
    JANUARY(1, "Styczeń"),
    FEBRUARY(2, "Luty"),
    MARCH(3, "Marzec"),
    APRIL(4, "Kwiecień"),
    MAY(5, "Maj"),
    JUNE(6, "Czerwiec"),
    JULY(7, "Lipiec"),
    AUGUST(8, "Sierpień"),
    SEPTEMBER(9, "Wrzesień"),
    OCTOBER(10, "Październik"),
    NOVEMBER(11, "Listopad"),
    DECEMBER(12, "Grudzień");

    companion object {
        fun fromNumber(number: Int): Month? = entries.find { it.number == number }
        fun fromString(dateString: String): Month? = entries.find {
            it.number == getNumberFromDateString(dateString)
        }
        fun getMonth(date:LocalDate): Month? = entries.find {
            it.number == date.month.number
        }

        private fun getNumberFromDateString(dateString: String): Int {
            try {
                return dateString.substringAfter("-").substringBeforeLast("-").toInt()
            } catch (e:Exception) {
                throw IllegalArgumentException("getNumberFromDateString: Wrong LocalDate String format")
            }
        }
    }
}
