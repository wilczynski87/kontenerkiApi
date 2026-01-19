package com.kontenery.controller

import com.kontenery.library.model.PaymentsListForFinanceTable
import com.kontenery.library.model.Product
import com.kontenery.library.utils.endOfCurrentYear
import com.kontenery.library.utils.startOfCurrentYear
import com.kontenery.service.ListingService
import io.ktor.http.headers
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import jdk.internal.net.http.common.Log.headers
import kotlinx.datetime.LocalDate

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
        authenticate("auth-jwt") {
            get("/clients/count") {
                val clientListSize: Long = listingService.clientsListSize()
                println("clientListCount: $clientListSize")
                headers {
                    append("Accept", "application/json")
                }

                call.respond(clientListSize)
            }
        }

        get("/clientsPayments") {
            val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
            val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100
            val fromRaw: String? = call.request.queryParameters["from"]
            val toRaw: String? = call.request.queryParameters["to"]

            val from: LocalDate = LocalDate.parse(fromRaw ?: LocalDate.startOfCurrentYear().toString())
            val to: LocalDate = LocalDate.parse(toRaw ?: LocalDate.endOfCurrentYear().toString())

            val clientListPayments: List<PaymentsListForFinanceTable> = listingService.clientsFinancesList(page, size, from, to)
            println("clientListPayments: $clientListPayments")


            call.respond(clientListPayments)
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