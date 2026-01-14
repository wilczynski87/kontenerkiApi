package com.kontenery.repository.impl

import com.kontenery.library.model.Product
import com.kontenery.repository.ProductRepo
import com.kontenery.repository.entity.*

class ProductRepoImpl: ProductRepo {
    // Save a new Container
    override suspend fun save(product: Product.Container): Product.Container = suspendTransaction {

        ProductEntity.new {
            name = product.name ?: Product.createProductName(product)
            location = product.location
            type = ProductType.CONTAINER
            client = product.client?.id?.let { ClientEntity.findById(it) }
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
    override suspend fun save(product: Product.Yard): Product.Yard = suspendTransaction {
        ProductEntity.new {
            name = product.name ?: Product.createProductName(product)
            location = product.location
            type = ProductType.YARD
            client = product.client?.id?.let { ClientEntity.findById(it) }
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
        else mapProductByType(prod)
    }

    // Update an existing Container
    override suspend fun updateProduct(product: Product.Container): Product.Container = suspendTransaction {
        val id:Long = product.id ?: throw NullPointerException("Nowy produkt nie ma ID: $product")
        ProductEntity.findByIdAndUpdate(id) { it ->
            it.apply {
                name = product.name ?: this.name ?: Product.createProductName(it.toContainer())
                location = product.location ?: this.location
                length = product.length ?: this.length
                client = product.client?.id?.let { ClientEntity.findById(it) } ?: this.client
                height = product.height ?: this.height
                color = product.color ?: this.color
                acquireDate = product.acquireDate ?: this.acquireDate
                lastPainting = product.lastPainting ?: this.lastPainting
                description = product.description ?: this.description
                photo = product.photo ?: this.photo
                type = ProductType.CONTAINER
            }
        }?.toContainer() ?: throw NullPointerException("Nie udało się uaktualnic: $product")
    }

    // Update an existing Yard
    override suspend fun updateProduct(product: Product.Yard): Product.Yard = suspendTransaction {
       val id:Long = product.id ?: throw NullPointerException("Nowy produkt nie ma ID: $product")
        ProductEntity.findByIdAndUpdate(id) {
            it.apply {
                name = product.name ?: this.name ?: Product.createProductName(it.toYard())
                location = product.location ?: this.location
                quantity = product.quantity ?: this.quantity
                client = product.client?.id?.let { ClientEntity.findById(it) } ?: this.client
                type = ProductType.YARD
            }
        }?.toYard() ?: throw NullPointerException("Nie udało się uaktualnic: $product")
    }

    override suspend fun releaseProduct(id: Long): Boolean = suspendTransaction {
        ProductEntity.findByIdAndUpdate(id) {
            it.apply {
                client = null
            }
        } != null
    }
}

private fun mapProductByType(productEntity: ProductEntity): Product {
    return when(productEntity.type) {
        ProductType.CONTAINER -> productEntity.toContainer()
        ProductType.YARD -> productEntity.toYard()
        else -> throw TypeCastException("Can not define proper Type in: mapProductByType()")
    }
}
