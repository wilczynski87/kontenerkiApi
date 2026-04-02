package com.kontenery.service

import com.kontenery.data.Product

interface ProductService {
    suspend fun save(product: Product): Product?
    suspend fun getAllProduct(page:Int, size:Int): List<Any>
    suspend fun getAllContainers(page:Int, size:Int): List<Product.Container>
    suspend fun getAllYards(page:Int, size:Int): List<Product.Yard>
    suspend fun findProductById(id:Long): Any?
    suspend fun updateProduct(product: Product): Any?
    suspend fun releaseProduct(id:Long): Boolean
}