package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.ClientCompanyData
import com.kontenery.data.invoice.InvoiceSend
import com.kontenery.data.utils.InvoiceType
import com.kontenery.data.utils.errors.InvoiceErrorMessage
import com.kontenery.ksef.service.KsefService
import com.kontenery.repository.BillRepo
import com.kontenery.repository.InvoiceRepo
import com.kontenery.service.ClientService
import com.kontenery.service.PrintService
import com.kontenery.testfixtures.sampleVatInvoice
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.rmi.ServerException

class InvoiceServiceSendPeriodicForAllTest {

    private lateinit var invoiceRepo: InvoiceRepo
    private lateinit var billRepo: BillRepo
    private lateinit var clientService: ClientService
    private lateinit var printService: PrintService
    private lateinit var ksefService: KsefService
    private lateinit var service: InvoiceServiceImpl

    private val period = LocalDate(2026, 8, 1)

    private val vatClient = Client(
        id = 21L,
        isActive = true,
        clientCompany = ClientCompanyData(name = "Aurora sp. z o.o.", needInvoice = true),
    )

    private val existingInvoice = sampleVatInvoice(invoiceNumber = "12/8/2026").copy(
        type = InvoiceType.PERIODIC.name,
        invoiceDate = period,
        vatApply = true,
    )

    @BeforeEach
    fun setUp() {
        invoiceRepo = mockk()
        billRepo = mockk()
        clientService = mockk()
        printService = mockk(relaxUnitFun = true)
        ksefService = mockk()
        service = InvoiceServiceImpl(
            invoiceRepo = invoiceRepo,
            billRepo = billRepo,
            clientService = clientService,
            productService = mockk(),
            contractService = mockk(),
            printService = printService,
            ksefService = { ksefService },
        )
    }

    @Test
    fun `resends email when PERIODIC already exists`() = runBlocking {
        coEvery { clientService.getFilteredClients(true) } returns listOf(vatClient)
        coEvery {
            invoiceRepo.getInvoicesForClient(0, 100, 21L, any(), any())
        } returns listOf(existingInvoice)
        coEvery { printService.sendInvoiceAgain(existingInvoice) } returns InvoiceSend(
            invoiceNumber = existingInvoice.invoiceNumber,
        )

        val errors = service.sendPeriodicInvoicesForAll(period)

        assertEquals(emptyList<InvoiceErrorMessage>(), errors)
        coVerify(exactly = 1) { printService.sendInvoiceAgain(existingInvoice) }
        coVerify(exactly = 0) { printService.sendPeriodicInvoice(any()) }
    }

    @Test
    fun `returns mail error when resend fails`() = runBlocking {
        coEvery { clientService.getFilteredClients(true) } returns listOf(vatClient)
        coEvery {
            invoiceRepo.getInvoicesForClient(0, 100, 21L, any(), any())
        } returns listOf(existingInvoice)
        coEvery { printService.sendInvoiceAgain(existingInvoice) } throws ServerException("mail down")

        val errors = service.sendPeriodicInvoicesForAll(period)

        assertEquals(1, errors.size)
        assertTrue(errors.first().message?.contains("mail down") == true)
    }

    @Test
    fun `returns accumulated errors when later client throws`() = runBlocking {
        val secondClient = vatClient.copy(id = 22L)

        coEvery { clientService.getFilteredClients(true) } returns listOf(vatClient, secondClient)
        coEvery {
            invoiceRepo.getInvoicesForClient(0, 100, 21L, any(), any())
        } returns listOf(existingInvoice)
        coEvery { printService.sendInvoiceAgain(existingInvoice) } throws ServerException("partial")
        coEvery {
            invoiceRepo.getInvoicesForClient(0, 100, 22L, any(), any())
        } throws RuntimeException("unexpected loop failure")

        val errors = service.sendPeriodicInvoicesForAll(period)

        assertEquals(1, errors.size)
        assertTrue(errors.first().message?.contains("partial") == true)
    }
}
