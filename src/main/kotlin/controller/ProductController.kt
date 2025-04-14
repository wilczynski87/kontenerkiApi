package com.kontenery.controller

import com.kontenery.model.*
import com.kontenery.service.ProductService
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRouting(productService: ProductService) {
    route("/products") {
        get("/allProducts") {
            println("GET ALL PRODUCT")
            val page: Int = call.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.queryParameters["size"]?.toInt() ?: 100
            try {

                val products: List<Any> = productService.getAllProduct(page, size)
                println("controller print: $products")

                val resp = products.map { it as JoiningInterface}.toList()

                call.respond(resp)
            } catch (e: Exception) {
                println(e)
            }
        }

        get("/findById/{id}") {
            println("FIND PRODUCT")
            val id:Long = call.pathParameters["id"]?.toLongOrNull() ?: throw BadRequestException("Invalid ID format")

            val product = productService.findProductById(id)
            println("controller log: $product")
            if(product is Yard) println("controller log YARD: $product")

            if (product == null) call.respond(HttpStatusCode.ExpectationFailed, "Nie ma takiego produktu")
            else call.respond(product)
        }

        post {
            try {
                println("POST PRODUCT")
                val newRawProduct = call.receive<String>()
                val newProduct = productDeserializer(newRawProduct) ?: throw IllegalArgumentException("Żle wprowadzony produkt")
                println("newProduct: $newProduct")
                val savedProduct = productService.save(newProduct)
                println("savedProduct: $savedProduct")

                if (savedProduct == null) call.respond(HttpStatusCode.ExpectationFailed, "Nie mogę zapisać produktu")
                else call.respond(savedProduct)

            } catch (e:Exception) {
                println(e)
            }
        }

        put {
            val newProduct = "ppp"
            val savedProduct = "ppp"

            if (savedProduct == null) call.respond(HttpStatusCode.ExpectationFailed, "Nie ma takiego produktu")
            else call.respond(savedProduct)
        }
    }
}