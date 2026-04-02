package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.Payment
import com.kontenery.data.utils.SellerAccount
import com.kontenery.model.CSVData
import com.kontenery.service.BankAccountService
import com.kontenery.service.CSVService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.toKotlinLocalDate
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

class CSVServiceImpl(
    private val bankAccountService: BankAccountService,
): CSVService {

    override suspend fun readCSV(csv: String): List<Payment> {

        return coroutineScope {
            csv.split("\n")
                .drop(1)
                .dropLast(1)
                .map { it.split(";") }
                .mapNotNull { list ->
                    async {
                        try {
                            fromCsvLine(list)
                        } catch (e: Exception) {
                            println("Nie mogę użyć lini $list,\n$e")
                            null
                        }
                    }
                }.awaitAll()
                .filterNotNull()
                .map { csvDate ->
                    async { csvDate.CSVtoPaymentMapper() }
                }.awaitAll()
        }
    }

    override suspend fun readCSVAlior(csv: String): List<Payment> {
        return coroutineScope {
            async {
                csv.lineSequence()
                    .dropWhile { !it.startsWith("Data transakcji") } // pomiń wiersze nagłówka banku
                    .filter { it.isNotBlank() }
                    .drop(1)// pomiń nagłówek CSV
                    .toList()
                    .mapNotNull { line ->
                            val parts = line.split(";")
                            try {
                                fromCsvLineAlior(parts).CSVtoPaymentMapper()
                            } catch (e: Exception) {
                                println("Błąd parsowania wiersza: $line → ${e.message}")
                                null
                            }
                    }
            }.await()
        }
    }
    private fun fromCsvLineAlior(list: List<String>): CSVData {
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        return CSVData(
            dataKsiegowania = java.time.LocalDate.parse(list[0], formatter).toKotlinLocalDate(),
            dataWaluty = java.time.LocalDate.parse(list[1], formatter).toKotlinLocalDate(),
            nadawcaOdbiorca = list[2],
            adresNadawcyOdbiorcy = list[2],
            rachunekZrodlowy = list[9],
            rachunekDocelowy = list[10],
            tytulem = list[4],
            kwotaOperacji = BigDecimal(list[5].replace(",", ".")),
            waluta = list[8],
            numerReferencyjny = "",
            typOperacji = "",
            kategoria = null,
        )
    }

    private fun fromCsvLine(list: List<String>): CSVData {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val isKategoria: Boolean = list.size == 12
        return CSVData(
            dataKsiegowania = java.time.LocalDate.parse(list[0], formatter).toKotlinLocalDate(),
            dataWaluty = java.time.LocalDate.parse(list[1], formatter).toKotlinLocalDate(),
            nadawcaOdbiorca = list[2],
            adresNadawcyOdbiorcy = list[3],
            rachunekZrodlowy = "PL" + list[4].filterNot { it == '\'' },
            rachunekDocelowy = "PL" + list[5].filterNot { it == '\'' },
            tytulem = list[6],
            kwotaOperacji = BigDecimal(list[7].filterNot { it.isWhitespace() }.replace(",", ".")),
            waluta = list[8],
            numerReferencyjny = list[9].filterNot { it == '\'' },
            typOperacji = list[10],
            kategoria = if (isKategoria) list[11] else null
        )
    }

    private suspend fun CSVData.CSVtoPaymentMapper(): Payment {
        val client: Client? = bankAccountService.findClientByAccountNumber(this.rachunekZrodlowy)
//        println("this.rachunekZrodlowy: ${this.rachunekZrodlowy} -> ${client?.getName()}")

        val trimmedBusinessAccount: String =
            SellerAccount.BUSSINESS.accountNumber.trim().filterNot { it.isWhitespace() }
        val isBusinessAccount: Boolean = this.rachunekDocelowy == trimmedBusinessAccount

        return Payment(
            amount = kwotaOperacji,
            date = dataKsiegowania,
            fromClient = client,
            method = typOperacji,
            toAccount = if (isBusinessAccount) SellerAccount.BUSSINESS else SellerAccount.PRIVATE,
            fromAccount = this.rachunekZrodlowy,
            title = tytulem,
            forInvoices = mutableListOf(),
            referenceNumber = numerReferencyjny
        )
    }
}