package com.kontenery.controller

import com.kontenery.library.model.Client
import com.kontenery.library.model.ClientOnList
import com.kontenery.library.model.Product
import com.kontenery.repository.ClientRepo
import com.kontenery.repository.ProductRepo
import com.kontenery.service.ListingService
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.math.BigDecimal

fun Route.listingRoute(
    listingService: ListingService
) {
    route("/list") {

        get("/clients") {
            val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100

            val clientList = listingService.clientsList(page, size)
            println("clientList: $clientList")

            call.respond(clientList)
        }



//        get("/allProducts") {
        get("/products") {
            println("GET ALL PRODUCT")
            val page: Int = call.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.queryParameters["size"]?.toInt() ?: 100
            try {

                val products: List<Any> = listingService.productList(page, size)
//                println("controller print: $products")

                val resp = products.map { it as Product }.toList()

                println("Products response: print: $resp")

                call.respond(resp)
            } catch (e: Exception) {
                println(e)
            }
        }


    }

}