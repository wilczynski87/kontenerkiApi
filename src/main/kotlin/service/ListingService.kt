package com.kontenery.service

import com.kontenery.library.model.ClientOnList
import com.kontenery.library.model.PaymentsListForFinanceTable
import com.kontenery.library.model.Product
import com.kontenery.library.utils.endOfCurrentYear
import com.kontenery.library.utils.now
import com.kontenery.library.utils.startOfCurrentYear
import com.kontenery.model.ClientBalance
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

interface ListingService {

    suspend fun clientsList(page: Int, size: Int): List<ClientOnList>

    suspend fun clientsListSize(): Long

    suspend fun productList(page: Int, size: Int): List<Product>

    suspend fun clientsFinancesList(
        page: Int = 0,
        size: Int = 100,
        from: LocalDate = LocalDate.startOfCurrentYear(),
        to: LocalDate = LocalDate.endOfCurrentYear(), 
    ): List<PaymentsListForFinanceTable>

    suspend fun clientOverdue(clientId: Long, from: LocalDate, to: LocalDate): BigDecimal?

    suspend fun clientsOverdue(from: LocalDate, to: LocalDate): Map<Long, Double?>
}