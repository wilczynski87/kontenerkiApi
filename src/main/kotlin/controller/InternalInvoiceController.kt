package com.kontenery.controller

import com.kontenery.data.dto.PeriodicAutoSendSkipResponse
import com.kontenery.service.AppSettingsService
import com.kontenery.service.InvoiceService
import com.kontenery.utils.ApiErrorResponse
import com.kontenery.utils.cookRawPeriod
import com.kontenery.utils.isValidInternalApiKey
import com.kontenery.utils.respondInternalError
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Internal (no JWT) invoice routes protected by X-Internal-Key / INTERNAL_API_KEY.
 * Used by host cron for monthly periodic invoice send.
 */
fun Route.internalInvoiceRoutes(
    invoiceService: InvoiceService,
    appSettingsService: AppSettingsService,
) {
    route("/internal/invoice") {
        post("/sendInvoices/forAll") {
            if (!call.isValidInternalApiKey()) {
                call.respond(HttpStatusCode.Unauthorized, ApiErrorResponse("Unauthorized"))
                return@post
            }
            try {
                if (!appSettingsService.isPeriodicInvoiceAutoSendEnabled()) {
                    call.respond(
                        HttpStatusCode.OK,
                        PeriodicAutoSendSkipResponse(
                            skipped = true,
                            reason = "periodic_auto_send_disabled",
                        ),
                    )
                    return@post
                }
                val periodRaw: String? = call.queryParameters["period"]
                val period = cookRawPeriod(periodRaw, "/internal/invoice/sendInvoices/forAll")
                call.respond(invoiceService.sendPeriodicInvoicesForAll(period))
            } catch (e: Exception) {
                call.respondInternalError(e, "Failed to send periodic invoices")
            }
        }
    }
}
