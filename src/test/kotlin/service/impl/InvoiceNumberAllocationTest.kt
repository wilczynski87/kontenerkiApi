package com.kontenery.service.impl

import com.kontenery.repository.BillRepo
import com.kontenery.repository.InvoiceRepo
import com.kontenery.service.ClientService
import com.kontenery.service.ContractService
import com.kontenery.service.ProductService
import com.kontenery.testfixtures.sampleVatInvoice
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InvoiceNumberAllocationTest {

    @Test
    fun `concurrent custom invoice creates get distinct sequential numbers`() = runBlocking {
        val invoiceRepo = mockk<InvoiceRepo>()
        // Same DB max every time — in-process cache under mutex must still advance
        coEvery { invoiceRepo.getLastInvoiceNumber() } returns null

        val service = InvoiceServiceImpl(
            invoiceRepo = invoiceRepo,
            billRepo = mockk(),
            clientService = mockk(),
            productService = mockk(),
            contractService = mockk(),
        )

        val created = (1..8).map {
            async {
                service.createCustomInvoice(sampleVatInvoice().copy(invoiceNumber = null))
            }
        }.awaitAll()

        val numbers = created.mapNotNull { it?.invoiceNumber }
        assertEquals(8, numbers.size)
        assertEquals(numbers.toSet().size, numbers.size)
    }

    @Test
    fun `bill numbers get distinct values with r suffix`() = runBlocking {
        val invoiceRepo = mockk<InvoiceRepo>()
        coEvery { invoiceRepo.getLastBillNumber() } returns null

        val service = InvoiceServiceImpl(
            invoiceRepo = invoiceRepo,
            billRepo = mockk<BillRepo>(),
            clientService = mockk<ClientService>(),
            productService = mockk<ProductService>(),
            contractService = mockk<ContractService>(),
        )

        val first = service.createCustomInvoice(sampleVatInvoice(vatApply = false).copy(invoiceNumber = null))
        val second = service.createCustomInvoice(sampleVatInvoice(vatApply = false).copy(invoiceNumber = null))

        assertTrue(first?.invoiceNumber?.endsWith('r') == true)
        assertTrue(second?.invoiceNumber?.endsWith('r') == true)
        assertNotEquals(first?.invoiceNumber, second?.invoiceNumber)
    }
}
