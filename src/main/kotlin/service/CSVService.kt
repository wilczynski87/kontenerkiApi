package com.kontenery.service

import com.kontenery.library.model.Payment

interface CSVService {

    suspend fun readCSV(csv: String): List<Payment>
}