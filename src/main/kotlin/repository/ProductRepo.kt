package com.kontenery.repository

import com.kontenery.data.Product

interface ProductRepo {

    suspend fun save(product: Product.Container): Product.Container?
    suspend fun save(product: Product.Yard): Product.Yard?
    suspend fun getAllProduct(page:Int, size:Int): List<Product>
    suspend fun findProductById(id:Long): Product?
    suspend fun updateProduct(product: Product.Container): Product.Container?
    suspend fun updateProduct(product: Product.Yard): Product.Yard?
    suspend fun releaseProduct(id:Long): Boolean


}