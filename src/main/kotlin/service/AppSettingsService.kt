package com.kontenery.service

interface AppSettingsService {
    suspend fun isPeriodicInvoiceAutoSendEnabled(): Boolean
    suspend fun setPeriodicInvoiceAutoSendEnabled(enabled: Boolean): Boolean
}

object AppSettingsKeys {
    const val PERIODIC_INVOICE_AUTO_SEND = "periodic_invoice_auto_send"
}
