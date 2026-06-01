package com.kontenery.controller

import com.kontenery.data.invoice.Invoice
import com.kontenery.data.utils.errors.ErrorMessage
import com.kontenery.data.utils.errors.InvoiceErrorMessage
import com.kontenery.ksef.dto.KsefSendInvoiceResponse
import com.kontenery.ksef.exception.KsefErrorMessages
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.service.KsefService
import com.kontenery.service.InvoiceService

internal suspend fun saveInvoiceWithOptionalKsef(
    createdInvoice: Invoice,
    invoiceService: InvoiceService,
    ksefService: KsefService,
    errorList: MutableList<ErrorMessage>? = null,
): Invoice? {
    var ksefResponse: KsefSendInvoiceResponse? = null
    val toSave = if (createdInvoice.vatApply) {
        try {
            val response = ksefService.sendInvoiceToKsef(createdInvoice).also { ksefResponse = it }
            println("ksefResponse initila send: $response")
            response
        } catch (e: KsefException) {
            if (errorList != null) {
                errorList.add(
                    InvoiceErrorMessage(
                        title = "Wysyłka do KSeF",
                        message = "Nie udało się wysłać faktury do KSeF: ${KsefErrorMessages.userMessage(e)}",
                        clientId = createdInvoice.customer?.client?.id,
                        period = createdInvoice.invoiceDate,
                    ),
                )
                return null
            }
            throw e
        }.let { createdInvoice.copy(ksefNumber = it.ksefNumber) }
    } else {
        createdInvoice
    }

    val saved = if (errorList != null) {
        invoiceService.saveInvoiceWithErrors(toSave.vatApply, toSave, errorList)
    } else {
        invoiceService.saveInvoice(toSave)
    } ?: return null

    ksefResponse?.sessionStatus?.let { status ->
        saved.invoiceNumber?.let { ksefService.persistSessionStatus(it, status) }
    }
    println("ksefResponse: $ksefResponse")
    println("saved: $saved")
    return saved
}
