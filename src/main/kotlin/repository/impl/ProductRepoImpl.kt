package com.kontenery.repository.impl

import com.kontenery.model.Product
import com.kontenery.model.Container
import com.kontenery.model.Yard
import com.kontenery.repository.ProductRepo
import com.kontenery.repository.entity.*

class ProductRepoImpl: ProductRepo {
    // Save a new Container
    override suspend fun save(product: Container): Container? = suspendTransaction {

        ProductEntity.new {
            name = product.name
            location = product.location
            type = ProductType.CONTAINER
            length = product.length
            height = product.height
            color = product.color
            acquireDate = product.acquireDate
            lastPainting = product.lastPainting
            description = product.description
            photo = product.photo
        }.toContainer()

    }

    // Save a new Yard
    override suspend fun save(product: Yard): Yard? = suspendTransaction {
        ProductEntity.new {
            name = product.name
            location = product.location
            type = ProductType.YARD
            quantity = product.quantity
        }.toYard()
    }

    // Get all products with pagination (Containers and Yards)
    override suspend fun getAllProduct(page: Int, size: Int): List<Product> = suspendTransaction {

        val countOffset:Long = (page * size).toLong()
        println("countOffset: $countOffset, size: $size")

        ProductEntity.all()
            .offset(countOffset)
            .limit(size)
            .map {
                mapProductByType(it)
            }
            .toList()
    }

    // Find a product by ID (either Container or Yard)
    override suspend fun findProductById(id: Long): Product? = suspendTransaction {
        val prod = ProductEntity.findById(id)

        if(prod == null) null
        else mapProductToType(prod)
    }

    // Update an existing Container
    override suspend fun updateProduct(product: Container): Container? = suspendTransaction {
//        return@withContext transaction {
//            val container = ContainerEntity.findById(product.id)
//            container?.apply {
//                name = product.name ?: this.name
//                location = product.location ?: this.location
//                length = product.length ?: this.length
//                height = product.height ?: this.height
//                color = product.color ?: this.color
//                acquireDate = product.acquireDate ?: this.acquireDate
//                lastPainting = product.lastPainting ?: this.lastPainting
//                description = product.description ?: this.description
//                photo = product.photo ?: this.photo
//            }
//        }
        null
    }

    // Update an existing Yard
    override suspend fun updateProduct(product: Yard): Yard? = suspendTransaction {
//        return@withContext transaction {
//            val yard = YardEntity.findById(product.id)
//            yard?.apply {
//                name = product.name ?: this.name
//                location = product.location ?: this.location
//                quantity = product.quantity ?: this.quantity
//            }
//        }
        null
    }
}

private fun mapProductByType(productEntity: ProductEntity): Product {
    return when(productEntity.type) {
        ProductType.CONTAINER -> productEntity.toContainer()
        ProductType.YARD -> productEntity.toYard()
        else -> productEntity.toProduct()
    }
}

private fun mapProductToType(productEntity: ProductEntity): Product {
    return when(productEntity.type) {
        ProductType.CONTAINER -> productEntity.toContainer()
        ProductType.YARD -> productEntity.toYard()
        else -> productEntity.toProduct()
    }
}
