package com.kontenery.service

import com.kontenery.data.Payment

interface CSVService {

    suspend fun readCSV(csv: String): List<Payment>
    suspend fun readCSVAlior(csv: String): List<Payment>
    suspend fun readCSVNest(csv: String): List<Payment>
}