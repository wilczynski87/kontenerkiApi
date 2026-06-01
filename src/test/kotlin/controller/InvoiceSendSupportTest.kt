package com.kontenery.controller

import com.kontenery.data.utils.errors.InvoiceErrorMessage
import com.kontenery.ksef.dto.KsefSendInvoiceResponse
import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse
import com.kontenery.ksef.dto.KsefStatusInfo
import com.kontenery.ksef.exception.KsefException
import com.kontenery.ksef.service.KsefService
import com.kontenery.service.InvoiceService
import com.kontenery.testfixtures.sampleNonVatInvoice
import com.kontenery.testfixtures.sampleVatInvoice
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InvoiceSendSupportTest {

    @Test
    fun `saves non-VAT invoice without calling KSeF`() = runBlocking {
        val invoice = sampleNonVatInvoice()
        val invoiceService = mockk<InvoiceService>()
        val ksefService = mockk<KsefService>()
        coEvery { invoiceService.saveInvoiceWithErrors(false, invoice, any()) } returns invoice

        val saved = saveInvoiceWithOptionalKsef(invoice, invoiceService, ksefService, mutableListOf())

        assertEquals(invoice, saved)
        coVerify(exactly = 0) { ksefService.sendInvoiceToKsef(any()) }
    }

    @Test
    fun `VAT invoice sends to KSeF then saves with ksef number`() = runBlocking {
        val invoice = sampleVatInvoice()
        val invoiceWithKsef = invoice.copy(ksefNumber = "KSeF-99")
        val sessionStatus = KsefSessionInvoiceStatusResponse(
            ksefNumber = "KSeF-99",
            invoiceNumber = invoice.invoiceNumber,
            status = KsefStatusInfo(code = 200, description = "OK"),
        )
        val invoiceService = mockk<InvoiceService>()
        val ksefService = mockk<KsefService>()
        coEvery { ksefService.persistSessionStatus(any(), any()) } returns Unit
        coEvery { ksefService.sendInvoiceToKsef(invoice) } returns KsefSendInvoiceResponse(
            sessionReferenceNumber = "sess-1",
            invoiceReferenceNumber = "inv-ref-1",
            ksefNumber = "KSeF-99",
            invoiceNumber = invoice.invoiceNumber,
            sessionStatus = sessionStatus,
        )
        coEvery {
            invoiceService.saveInvoiceWithErrors(true, invoiceWithKsef, any())
        } returns invoiceWithKsef

        val saved = saveInvoiceWithOptionalKsef(invoice, invoiceService, ksefService, mutableListOf())

        assertEquals("KSeF-99", saved?.ksefNumber)
        coVerify { ksefService.persistSessionStatus(invoice.invoiceNumber!!, sessionStatus) }
    }

    @Test
    fun `KSeF failure adds error and skips save when error list provided`() = runBlocking {
        val invoice = sampleVatInvoice()
        val errors = mutableListOf<com.kontenery.data.utils.errors.ErrorMessage>()
        val invoiceService = mockk<InvoiceService>()
        val ksefService = mockk<KsefService>()
        coEvery { ksefService.sendInvoiceToKsef(invoice) } throws KsefException("KSeF invoice processing failed: 450")

        val saved = saveInvoiceWithOptionalKsef(invoice, invoiceService, ksefService, errors)

        assertNull(saved)
        assertEquals(1, errors.size)
        assertTrue(errors.single() is InvoiceErrorMessage)
        coVerify(exactly = 0) { invoiceService.saveInvoiceWithErrors(any(), any(), any()) }
    }

    @Test
    fun `KSeF failure propagates when error list is null`() {
        val invoice = sampleVatInvoice()
        val invoiceService = mockk<InvoiceService>()
        val ksefService = mockk<KsefService>()
        coEvery { ksefService.sendInvoiceToKsef(invoice) } throws KsefException("processing failed")

        assertThrows(KsefException::class.java) {
            runBlocking { saveInvoiceWithOptionalKsef(invoice, invoiceService, ksefService, null) }
        }
    }

    @Test
    fun `returns null when save fails`() = runBlocking {
        val invoice = sampleNonVatInvoice()
        val invoiceService = mockk<InvoiceService>()
        val ksefService = mockk<KsefService>()
        coEvery { invoiceService.saveInvoiceWithErrors(false, invoice, any()) } returns null

        val saved = saveInvoiceWithOptionalKsef(invoice, invoiceService, ksefService, mutableListOf())

        assertNull(saved)
    }
}
