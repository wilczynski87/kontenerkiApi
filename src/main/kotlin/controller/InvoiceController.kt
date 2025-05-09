package com.kontenery.controller

import com.kontenery.service.InvoiceService
import io.ktor.server.routing.*

fun Route.invoiceRoutes(invoiceService: InvoiceService) {
    route("invoice") {
        get("/{invoiceId}/id") {

        }
        get("/forDate") {

        }
        get("/{clientId}/forClient") {

        }
        post {

        }
    }

}