package com.kontenery.controller

import com.kontenery.library.model.Product
import com.kontenery.service.ProductService
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRouting(productService: ProductService) {
    route("/products") {

        get("/containers") {
            val page: Int = call.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.queryParameters["size"]?.toInt() ?: 100
            try {

                val containers: List<Product.Container> = productService.getAllContainers(page, size)
                println("controller print: $containers")

                call.respond(containers)

            } catch (e: Exception) {
                println(e)
            }
        }

        get("/yards") {
            val page: Int = call.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.queryParameters["size"]?.toInt() ?: 100
            try {

                val yards: List<Product.Yard> = productService.getAllYards(page, size)
                println("controller print: $yards")

                call.respond(yards)

            } catch (e: Exception) {
                println(e)
            }
        }

        get("/findById/{id}") {
            println("FIND PRODUCT")
            val id:Long = call.pathParameters["id"]?.toLongOrNull() ?: throw BadRequestException("Invalid ID format")

            val product = productService.findProductById(id)
            println("controller log: $product")
            if(product is Product.Yard) println("controller log YARD: $product")

            if (product == null) call.respond(HttpStatusCode.ExpectationFailed, "Nie ma takiego produktu")
            else call.respond(product)
        }

        post("/yard") {
            try {
                println("POST YARD")
                val newYard = call.receive<Product.Yard>()
                println("newYard: $newYard")
                val savedProduct = productService.save(newYard)
                println("savedProduct: $savedProduct")

                if (savedProduct == null) call.respond(HttpStatusCode.ExpectationFailed, "Nie mogę zapisać produktu: $newYard")
                else call.respond(savedProduct)

            } catch (e:Exception) {
                println(e)
                call.respond(HttpStatusCode.ExpectationFailed, "Nie mogę zapisać produktu: $e")
            }
        }
        post("/container") {
            try {
                println("POST CONTAINER")
                val newContainer = call.receive<Product.Container>()
                println("newProduct: $newContainer")
                val savedProduct = productService.save(newContainer)
                println("savedProduct: $savedProduct")

                if (savedProduct == null) call.respond(HttpStatusCode.ExpectationFailed, "Nie mogę zapisać produktu: $newContainer")
                else call.respond(savedProduct)

            } catch (e:Exception) {
                println(e)
                call.respond(HttpStatusCode.ExpectationFailed, "Nie mogę zapisać produktu: $e")
            }
        }

        put("/yard/{id}") {
            try {
                println("PUT YARD")
                val id:Long = call.pathParameters["id"]?.toLongOrNull() ?: throw BadRequestException("Invalid ID format")
                val newYard = call.receive<Product.Yard>()
                println("newProduct: $newYard")
                val updatedProduct: Product.Yard = productService.updateProduct(newYard) as Product.Yard
                println("updatedProduct: $updatedProduct")

                call.respond(updatedProduct)

            } catch (e:Exception) {
                println(e)
            }
        }

        put("/container/{id}") {
            try {
                println("PUT CONTAINER")
                val id:Long = call.pathParameters["id"]?.toLongOrNull() ?: throw BadRequestException("Invalid ID format")
                val newContainer = call.receive<Product.Container>()
                println("newProduct: $newContainer")
                val updatedProduct: Product.Container = productService.updateProduct(newContainer) as Product.Container
                println("updatedProduct: $updatedProduct")

                call.respond(updatedProduct)

            } catch (e:Exception) {
                println(e)
            }
        }

        post("/container/all") {
            try {
                val containers = call.receive<List<Product.Container>>()
                containers.forEach {
                    println(it)
                }
                containers.forEach {
                    productService.save(it)
                }

                call.respond("ok")
            } catch (e: Exception) {
                println(e)
            }
        }

        post("/yard/all") {
            try {
                val containers = call.receive<List<Product.Yard>>()
                containers.forEach {
                    println(it)
                }
                containers.forEach {
                    productService.save(it)
                }

                call.respond("ok")
            } catch (e: Exception) {
                println(e)
            }
        }
    }
}