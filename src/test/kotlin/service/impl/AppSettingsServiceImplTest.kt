package com.kontenery.service.impl

import com.kontenery.repository.AppSettingsRepo
import com.kontenery.service.AppSettingsKeys
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppSettingsServiceImplTest {

    @Test
    fun `isPeriodicInvoiceAutoSendEnabled defaults to false when unset`() = runBlocking {
        val repo = mockk<AppSettingsRepo>()
        coEvery { repo.get(AppSettingsKeys.PERIODIC_INVOICE_AUTO_SEND) } returns null

        val service = AppSettingsServiceImpl(repo)
        assertFalse(service.isPeriodicInvoiceAutoSendEnabled())
    }

    @Test
    fun `isPeriodicInvoiceAutoSendEnabled accepts true variants`() = runBlocking {
        val repo = mockk<AppSettingsRepo>()
        coEvery { repo.get(AppSettingsKeys.PERIODIC_INVOICE_AUTO_SEND) } returnsMany listOf("true", "1", "YES")

        val service = AppSettingsServiceImpl(repo)
        assertTrue(service.isPeriodicInvoiceAutoSendEnabled())
        assertTrue(service.isPeriodicInvoiceAutoSendEnabled())
        assertTrue(service.isPeriodicInvoiceAutoSendEnabled())
    }

    @Test
    fun `setPeriodicInvoiceAutoSendEnabled persists string`() = runBlocking {
        val repo = mockk<AppSettingsRepo>(relaxUnitFun = true)

        val service = AppSettingsServiceImpl(repo)
        assertTrue(service.setPeriodicInvoiceAutoSendEnabled(true))

        coVerify(exactly = 1) {
            repo.set(AppSettingsKeys.PERIODIC_INVOICE_AUTO_SEND, "true")
        }
    }
}
