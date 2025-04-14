package com.kontenery.repository

import com.kontenery.model.Container
import com.kontenery.model.Product
import com.kontenery.model.Yard

interface ProductRepo {

    suspend fun save(product: Container): Container?

    suspend fun save(product: Yard): Yard?

    suspend fun getAllProduct(page:Int, size:Int): List<Product>

    suspend fun findProductById(id:Long): Product?

    suspend fun updateProduct(product: Container): Container?

    suspend fun updateProduct(product: Yard): Yard?
}