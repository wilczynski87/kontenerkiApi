package com.kontenery.data.utils

import java.math.BigInteger

enum class SellerAccount(val accountNumber: String) {
    BUSSINESS("PL50 1950 0001 2006 0023 6241 0001"),
    PRIVATE("PL11 2490 1044 0000 4200 8845 2192");

    companion object {
        fun fromAccountNumber(value: String): SellerAccount? {
            return entries.firstOrNull { it1 ->
                it1.accountNumber
                    .trim()
                    .filterNot { it.isWhitespace() } == value.trim().filterNot { it.isWhitespace() } }
        }
    }
}

class BankAccount {
    companion object {

        fun toPolishIbanFormatted(countryCode: String = "PL", rawNrb: String): String {
            // Usuń spacje i znaki niebędące cyframi
            val nrb = rawNrb.filter { it.isDigit() }

            if (nrb.length != 26) {
                throw IllegalArgumentException("NRB musi mieć dokładnie 26 cyfr.")
            }

            // Oblicz sumę kontrolną IBAN
            val checksum = calculateIbanChecksum(countryCode, nrb)

            // Zbuduj pełny IBAN
            val iban = "PL$checksum$nrb"

            // Sformatuj: odstępy co 4 znaki
            return iban.chunked(4).joinToString(" ")
        }

        private fun calculateIbanChecksum(countryCode: String, nrb: String): String {
            // Przesunięcie: numer konta + kod kraju + 00
            val rearranged = nrb + countryCode + "00"

            // Zamiana liter na liczby (A = 10, ..., Z = 35)
            val numeric = rearranged.map {
                when (it) {
                    in '0'..'9' -> it.toString()
                    in 'A'..'Z' -> (it.code - 'A'.code + 10).toString()
                    else -> error("Nieprawidłowy znak w numerze IBAN")
                }
            }.joinToString("")

            // MOD 97
            val mod = BigInteger(numeric) % BigInteger.valueOf(97)
            val checkDigits = 98 - mod.toInt()

            return checkDigits.toString().padStart(2, '0')
        }
    }
}

