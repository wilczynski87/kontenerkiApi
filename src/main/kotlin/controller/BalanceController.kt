package com.kontenery.controller

import com.kontenery.library.utils.now
import com.kontenery.model.PrevYearBalance
import com.kontenery.service.ClientService
import com.kontenery.utils.startOfYear
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

fun Route.balance(
    clientService: ClientService,

) {
    route("balance") {
        get("/client/{id}") {
            try {
                val clientId: Long = call.parameters["id"]?.toLongOrNull() ?: throw NullPointerException("Nie mogę odczytać ID")
                val from: LocalDate = call.request.queryParameters["from"]?.let { LocalDate.parse(it) }
                    ?: LocalDate.now().minus(5, DateTimeUnit.YEAR)
                val to: LocalDate = call.request.queryParameters["to"]?.let { LocalDate.parse(it) }
                    ?: LocalDate.now().startOfYear()

                // znajdź klienta i sprawdź czy istnieje

                // zczytaj faktury i rachnki z poprzednich lat

                // zczytaj wpływy z ostatnich lat

                // utwórz PrevYearBalance i wypełnij

                val balance = PrevYearBalance()

                call.respond(balance)

            } catch (e: Exception) {
                println("balance, client/{id}: $e")
                call.respond(HttpStatusCode.ExpectationFailed, e)
            }

        }
    }

}