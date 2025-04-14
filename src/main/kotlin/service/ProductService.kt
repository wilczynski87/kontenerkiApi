package com.kontenery.service

import com.kontenery.model.Client
import com.kontenery.model.Product

interface ProductService {

    suspend fun save(product: Product): Any?

    suspend fun getAllProduct(page:Int, size:Int): List<Any>

    suspend fun findProductById(id:Long): Any?

    suspend fun updateProduct(product: Product): Any?
}