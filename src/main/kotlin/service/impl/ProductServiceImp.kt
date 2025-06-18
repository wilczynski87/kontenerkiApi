package com.kontenery.service.impl

import com.kontenery.library.model.Product
import com.kontenery.repository.ProductRepo
import com.kontenery.service.ProductService

class ProductServiceImp(private val productRepo: ProductRepo): ProductService {
    override suspend fun save(product: Product): Product? {
        return when(product) {
            is Product.Container -> productRepo.save(product)
            is Product.Yard -> productRepo.save(product)
            else -> throw IllegalArgumentException("Nie ma takiego rodzaju produktu")
        }
    }

    override suspend fun getAllProduct(page: Int, size: Int): List<Product> {
        return productRepo.getAllProduct(page, size)
    }

    override suspend fun getAllContainers(page: Int, size: Int): List<Product.Container> {
        return productRepo.getAllProduct(page, size)
            .filterIsInstance<Product.Container>()
            .map { product: Product ->
                product as Product.Container
            }
    }

    override suspend fun getAllYards(page: Int, size: Int): List<Product.Yard> {
        return productRepo.getAllProduct(page, size)
            .filterIsInstance<Product.Yard>()
            .map { product: Product ->
                product as Product.Yard
            }
    }

    override suspend fun findProductById(id: Long): Product? {
        return productRepo.findProductById(id)
    }

    override suspend fun updateProduct(product: Product): Product? {
        return when(product) {
            is Product.Container -> productRepo.updateProduct(product)
            is Product.Yard -> productRepo.updateProduct(product)
            else -> throw IllegalArgumentException("Nie ma takiego rodzaju produktu")
        }
    }

}