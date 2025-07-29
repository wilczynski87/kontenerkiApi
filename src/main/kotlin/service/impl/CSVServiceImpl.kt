package com.kontenery.service.impl

import com.kontenery.library.model.Client
import com.kontenery.library.model.Payment
import com.kontenery.library.utils.SellerAccount
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
                .map { list ->
                    async {
                        try {
                            fromCsvLine(list)
                        } catch (e: Exception) {
                            println("Nie mogę użyć lini")
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

        val trimedBussinesAccount: String =
            SellerAccount.BUSSINESS.accountNumber.trim().filterNot { it.isWhitespace() }
        val isBussinesAccount: Boolean = this.rachunekDocelowy == trimedBussinesAccount

        return Payment(
            amount = kwotaOperacji,
            date = dataKsiegowania,
            fromClient = client,
            method = typOperacji,
            toAccount = if (isBussinesAccount) SellerAccount.BUSSINESS else SellerAccount.PRIVATE,
            fromAccount = this.rachunekZrodlowy,
            title = tytulem,
            forInvoices = mutableListOf(),
        )
    }
}