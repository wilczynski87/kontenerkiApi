package com.kontenery.controller

import com.kontenery.data.PaymentsListForFinanceTable
import com.kontenery.data.PaymentsListForFinanceTableWithBalance
import com.kontenery.data.Product
import com.kontenery.data.utils.endOfCurrentYear
import com.kontenery.data.utils.now
import com.kontenery.data.utils.startOfCurrentYear
import com.kontenery.service.ListingService
import io.ktor.http.headers
import io.ktor.server.auth.authenticate
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus


fun Route.listingRoute(
    listingService: ListingService
) {
    route("/list") {

        authenticate("auth-jwt") {
            
            get("/clients") {
                val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
                val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100

                val clientList = listingService.clientsList(page, size)
    //            println("clientList: $clientList")

                call.respond(clientList)
            }

            get("/clients/count") {
                val clientListSize: Long = listingService.clientsListSize()
                headers {
                    append("Accept", "application/json")
                }
                call.respond(clientListSize)
            }

            get("/clientsPayments") {
                val page: Int = call.request.queryParameters["page"]?.toInt() ?: 0
                val size: Int = call.request.queryParameters["size"]?.toInt() ?: 100
                val fromRaw: String? = call.request.queryParameters["from"]
                val toRaw: String? = call.request.queryParameters["to"]

//                println("page: $page, size: $size, from: $fromRaw, to: $toRaw")

                val from: LocalDate = LocalDate.parse(fromRaw ?: LocalDate.startOfCurrentYear().toString())
                val to: LocalDate = LocalDate.parse(toRaw ?: LocalDate.endOfCurrentYear().toString())

//                println("from: $from, to: $to")

                val clientListPayments: List<PaymentsListForFinanceTable> = listingService.clientsFinancesList(page, size, from, to)

                val clientListBalance = listingService.clientsOverdue(from.minus(5, DateTimeUnit.YEAR), from.minus(1, DateTimeUnit.DAY))

                val endOfPreviousMonth = LocalDate(to.year, to.monthNumber, 1).minus(1, DateTimeUnit.DAY)
                val clientsOverdueTo = if(to.year == LocalDate.now().year) endOfPreviousMonth else to
                val clientsOverdue = listingService.clientsOverdue(from.minus(5, DateTimeUnit.YEAR), clientsOverdueTo)

                val clientListPaymentsAndBalance: List<PaymentsListForFinanceTableWithBalance> = clientListPayments.map {
                    PaymentsListForFinanceTableWithBalance(
                        it.client,
                        it.payments,
                        clientListBalance[it.client?.clientId],
                        clientsOverdue[it.client?.clientId])
                }

                call.respond(clientListPaymentsAndBalance)
            }



    //        get("/allProducts") {
            get("/products") {
                val page: Int = call.queryParameters["page"]?.toInt() ?: 0
                val size: Int = call.queryParameters["size"]?.toInt() ?: 100
                val products: List<Any> = listingService.productList(page, size)
                val resp = products.map { it as Product }.toList()
                call.respond(resp)
            }

        }
    }
}