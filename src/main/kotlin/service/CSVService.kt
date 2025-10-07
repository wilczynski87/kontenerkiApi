package com.kontenery.service

import com.kontenery.library.model.Payment

interface CSVService {

    suspend fun readCSV(csv: String): List<Payment>
    suspend fun readCSVAlior(csv: String): List<Payment>
}