package com.kontenery.model

import kotlinx.datetime.LocalDate
import java.math.BigDecimal

data class CSVData(
    val dataKsiegowania: LocalDate,
    val dataWaluty: LocalDate,
    val nadawcaOdbiorca: String,
    val adresNadawcyOdbiorcy: String,
    val rachunekZrodlowy: String,
    val rachunekDocelowy: String,
    val tytulem: String,
    val kwotaOperacji: BigDecimal,
    val waluta: String,
    val numerReferencyjny: String,
    val typOperacji: String,
    val kategoria: String?
)
