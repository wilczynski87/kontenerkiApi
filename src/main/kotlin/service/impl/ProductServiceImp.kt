package com.kontenery.service.impl

import com.kontenery.model.Container
import com.kontenery.model.Product
import com.kontenery.model.Yard
import com.kontenery.repository.ProductRepo
import com.kontenery.service.ProductService

class ProductServiceImp(private val productRepo: ProductRepo): ProductService {
    override suspend fun save(product: Product): Product? {
        return when(product) {
            is Container -> productRepo.save(product)
            is Yard -> productRepo.save(product)
            else -> throw IllegalArgumentException("Nie ma takiego rodzaju produktu")
        }
    }

    override suspend fun getAllProduct(page: Int, size: Int): List<Product> {
        return productRepo.getAllProduct(page, size)
    }

    override suspend fun getAllContainers(page: Int, size: Int): List<Container> {
        return productRepo.getAllProduct(page, size)
            .filterIsInstance<Container>()
            .map { product: Product ->
                product as Container
            }
    }

    override suspend fun getAllYards(page: Int, size: Int): List<Yard> {
        return productRepo.getAllProduct(page, size)
            .filterIsInstance<Yard>()
            .map { product: Product ->
                product as Yard
            }
    }

    override suspend fun findProductById(id: Long): Product? {
        return productRepo.findProductById(id)
    }

    override suspend fun updateProduct(product: Product): Product? {
        return when(product) {
            is Container -> productRepo.updateProduct(product)
            is Yard -> productRepo.updateProduct(product)
            else -> throw IllegalArgumentException("Nie ma takiego rodzaju produktu")
        }
    }

}