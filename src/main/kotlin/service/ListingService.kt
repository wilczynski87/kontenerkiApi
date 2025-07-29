package com.kontenery.service

import com.kontenery.library.model.ClientOnList
import com.kontenery.library.model.Product

interface ListingService {

    suspend fun clientsList(page: Int, size: Int): List<ClientOnList>

    suspend fun productList(page: Int, size: Int): List<Product>
}