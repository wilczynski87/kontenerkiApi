package com.kontenery.service.impl

import com.kontenery.data.utils.InvoiceType
import com.kontenery.repository.BillRepo
import com.kontenery.repository.InvoiceRepo
import com.kontenery.service.ClientService
import com.kontenery.service.ContractService
import com.kontenery.service.ProductService
import com.kontenery.testfixtures.sampleNonVatInvoice
import com.kontenery.testfixtures.sampleVatInvoice
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InvoiceServiceFindPeriodicTest {

    private lateinit var invoiceRepo: InvoiceRepo
    private lateinit var billRepo: BillRepo
    private lateinit var service: InvoiceServiceImpl

    private val period = LocalDate(2026, 8, 15)

    @BeforeEach
    fun setUp() {
        invoiceRepo = mockk()
        billRepo = mockk()
        service = InvoiceServiceImpl(
            invoiceRepo = invoiceRepo,
            billRepo = billRepo,
            clientService = mockk<ClientService>(),
            productService = mockk<ProductService>(),
            contractService = mockk<ContractService>(),
        )
    }

    @Test
    fun `findPeriodicDocumentForClient returns PERIODIC VAT invoice`() = runBlocking {
        val periodic = sampleVatInvoice("12/8/2026").copy(
            type = InvoiceType.PERIODIC.name,
            vatApply = true,
        )
        val other = sampleVatInvoice("99/8/2026").copy(
            type = InvoiceType.UTILITIES.name,
            vatApply = true,
        )
        coEvery {
            invoiceRepo.getInvoicesForClient(0, 100, 21L, LocalDate(2026, 8, 1), LocalDate(2026, 8, 31))
        } returns listOf(other, periodic)

        val found = service.findPeriodicDocumentForClient(21L, period, vatApply = true)

        assertEquals("12/8/2026", found?.invoiceNumber)
        coVerify(exactly = 0) { billRepo.getBillsForClient(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `findPeriodicDocumentForClient returns PERIODIC bill when vatApply false`() = runBlocking {
        val bill = sampleNonVatInvoice("26/8/2026r").copy(type = InvoiceType.PERIODIC.name)
        coEvery {
            billRepo.getBillsForClient(0, 100, 7L, LocalDate(2026, 8, 1), LocalDate(2026, 8, 31))
        } returns listOf(bill)

        val found = service.findPeriodicDocumentForClient(7L, period, vatApply = false)

        assertEquals("26/8/2026r", found?.invoiceNumber)
        coVerify(exactly = 0) { invoiceRepo.getInvoicesForClient(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `findPeriodicDocumentForClient returns null when only non-PERIODIC exist`() = runBlocking {
        coEvery {
            invoiceRepo.getInvoicesForClient(0, 100, 21L, LocalDate(2026, 8, 1), LocalDate(2026, 8, 31))
        } returns listOf(
            sampleVatInvoice("1/8/2026").copy(type = InvoiceType.OTHER.name),
        )

        assertNull(service.findPeriodicDocumentForClient(21L, period, vatApply = true))
        assertFalse(service.hasPeriodicDocumentForClient(21L, period, vatApply = true))
    }

    @Test
    fun `hasPeriodicDocumentForClient is true when PERIODIC exists`() = runBlocking {
        coEvery {
            invoiceRepo.getInvoicesForClient(0, 100, 21L, LocalDate(2026, 8, 1), LocalDate(2026, 8, 31))
        } returns listOf(
            sampleVatInvoice("12/8/2026").copy(type = InvoiceType.PERIODIC.name),
        )

        assertTrue(service.hasPeriodicDocumentForClient(21L, period, vatApply = true))
    }
}
