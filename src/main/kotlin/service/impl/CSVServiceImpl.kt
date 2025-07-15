package com.kontenery.service.impl

import com.kontenery.model.CSVData
import com.kontenery.service.CSVService
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class CSVServiceImpl: CSVService {

    override fun readLinesFromSCV(csv: String) {
//        val csvLines: List<String> = csv.split("\n")
//        csvLines.map { line -> line.trim() }
//        val CSVDatas: List<CSVData> = csvLines
//            .drop(1)
//            .dropLast(1)
//            .map { line ->
//                fromCsvLine(line)
//            }
        val CSVDatas: List<CSVData> = csv
            .split("\n")
            .drop(1)
            .dropLast(1)
            .map { line ->
                fromCsvLine(line.trim())
            }
        println(CSVDatas[0])

    }

    private fun fromCsvLine(line: String): CSVData {
        val parts = line.split(";")
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val isKategoria: Boolean = parts.size == 12
        return CSVData(
            dataKsiegowania = java.time.LocalDate.parse(parts[0], formatter).toKotlinLocalDate(),
            dataWaluty = java.time.LocalDate.parse(parts[1], formatter).toKotlinLocalDate(),
            nadawcaOdbiorca = parts[2],
            adresNadawcyOdbiorcy = parts[3],
            rachunekZrodlowy = parts[4].filterNot{ it == '\'' },
            rachunekDocelowy = parts[5].filterNot{ it == '\'' },
            tytulem = parts[6],
            kwotaOperacji = BigDecimal(parts[7].filterNot { it.isWhitespace() }.replace(",", ".")),
            waluta = parts[8],
            numerReferencyjny = parts[9].filterNot{ it == '\'' },
            typOperacji = parts[10],
            kategoria = if(isKategoria) parts[11] else null
        )
    }
}