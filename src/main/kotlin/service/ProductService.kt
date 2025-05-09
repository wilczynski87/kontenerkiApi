package com.kontenery.service

import com.kontenery.model.Client
import com.kontenery.model.Container
import com.kontenery.model.Product
import com.kontenery.model.Yard

interface ProductService {

    suspend fun save(product: Product): Any?

    suspend fun getAllProduct(page:Int, size:Int): List<Any>

    suspend fun getAllContainers(page:Int, size:Int): List<Container>

    suspend fun getAllYards(page:Int, size:Int): List<Yard>

    suspend fun findProductById(id:Long): Any?

    suspend fun updateProduct(product: Product): Any?
}