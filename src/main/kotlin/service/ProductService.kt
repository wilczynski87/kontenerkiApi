package com.kontenery.service

import com.kontenery.model.Product

interface ProductService {

    suspend fun save(product: Product): Any?

    suspend fun getAllProduct(page:Int, size:Int): List<Any>

    suspend fun getAllContainers(page:Int, size:Int): List<Product.Container>

    suspend fun getAllYards(page:Int, size:Int): List<Product.Yard>

    suspend fun findProductById(id:Long): Any?

    suspend fun updateProduct(product: Product): Any?
}