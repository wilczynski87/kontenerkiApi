package com.kontenery.service.impl

import com.kontenery.repository.AppSettingsRepo
import com.kontenery.service.AppSettingsKeys
import com.kontenery.service.AppSettingsService

class AppSettingsServiceImpl(
    private val appSettingsRepo: AppSettingsRepo,
) : AppSettingsService {

    override suspend fun isPeriodicInvoiceAutoSendEnabled(): Boolean =
        appSettingsRepo.get(AppSettingsKeys.PERIODIC_INVOICE_AUTO_SEND)
            ?.trim()
            ?.lowercase()
            .let { it == "true" || it == "1" || it == "yes" }

    override suspend fun setPeriodicInvoiceAutoSendEnabled(enabled: Boolean): Boolean {
        appSettingsRepo.set(AppSettingsKeys.PERIODIC_INVOICE_AUTO_SEND, enabled.toString())
        return enabled
    }
}
