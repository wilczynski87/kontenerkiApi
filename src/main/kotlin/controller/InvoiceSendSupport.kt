package com.kontenery.controller

import com.kontenery.data.invoice.Invoice
import com.kontenery.data.utils.errors.ErrorMessage
import com.kontenery.ksef.service.KsefService
import com.kontenery.service.InvoiceService
import com.kontenery.service.parseKsefPermanentStorageDate as parseKsefPermanentStorageDateService
import com.kontenery.service.saveInvoiceWithOptionalKsef as saveInvoiceWithOptionalKsefService

/** Delegates to [com.kontenery.service.parseKsefPermanentStorageDate]. */
internal fun parseKsefPermanentStorageDate(date: String) =
    parseKsefPermanentStorageDateService(date)

/** Delegates to [com.kontenery.service.saveInvoiceWithOptionalKsef] for existing controller call sites. */
internal suspend fun saveInvoiceWithOptionalKsef(
    createdInvoice: Invoice,
    invoiceService: InvoiceService,
    ksefService: KsefService,
    errorList: MutableList<ErrorMessage>? = null,
): Invoice? = saveInvoiceWithOptionalKsefService(createdInvoice, invoiceService, ksefService, errorList)
