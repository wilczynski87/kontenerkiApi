package com.kontenery.service.impl

import com.kontenery.GateConfig
import com.kontenery.data.Contract
import com.kontenery.data.invoice.Invoice
import com.kontenery.repository.BillRepo
import com.kontenery.repository.GateEventRepo
import com.kontenery.repository.InvoiceRepo
import com.kontenery.service.ContractService
import com.kontenery.service.GateAccessDeniedException
import com.kontenery.service.ListingService
import com.kontenery.service.SuplaTokenProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class GateServiceImplTest {

    private lateinit var contractService: ContractService
    private lateinit var listingService: ListingService
    private lateinit var invoiceRepo: InvoiceRepo
    private lateinit var billRepo: BillRepo
    private lateinit var gateEventRepo: GateEventRepo
    private lateinit var suplaTokenProvider: SuplaTokenProvider
    private lateinit var service: GateServiceImpl

    @BeforeEach
    fun setUp() {
        contractService = mockk()
        listingService = mockk()
        invoiceRepo = mockk()
        billRepo = mockk()
        gateEventRepo = mockk(relaxUnitFun = true)
        suplaTokenProvider = mockk()
        service = GateServiceImpl(
            gateConfig = GateConfig(
                openUrl = "",
                mockMode = true,
                cooldownSeconds = 60,
            ),
            contractService = contractService,
            listingService = listingService,
            invoiceRepo = invoiceRepo,
            billRepo = billRepo,
            gateEventRepo = gateEventRepo,
            suplaTokenProvider = suplaTokenProvider,
        )
    }

    @Nested
    inner class CheckUserAuthenticated {
        @Test
        fun `returns client id for numeric userId`() {
            assertEquals(42L, service.checkUserAuthenticated("42"))
        }

        @Test
        fun `throws when userId is null`() {
            val ex = assertThrows<GateAccessDeniedException> {
                service.checkUserAuthenticated(null)
            }
            assertTrue(ex.message!!.contains("zalogowany", ignoreCase = true))
        }

        @Test
        fun `throws when userId is not numeric`() {
            assertThrows<GateAccessDeniedException> {
                service.checkUserAuthenticated("abc")
            }
        }

        @Test
        fun `accepts zero userId as numeric client id`() {
            // Admin/dev login (ppp) currently issues userId=0; gate auth only checks format.
            assertEquals(0L, service.checkUserAuthenticated("0"))
        }
    }

    @Nested
    inner class EnsureActiveContract {
        @Test
        fun `passes when client has active contract`() = runTest {
            coEvery { contractService.getByClientId(1L, onlyActive = true) } returns listOf(
                Contract(id = 10L),
            )
            service.ensureActiveContract(1L)
        }

        @Test
        fun `throws when no active contract`() = runTest {
            coEvery { contractService.getByClientId(1L, onlyActive = true) } returns emptyList()
            val ex = assertThrows<GateAccessDeniedException> {
                service.ensureActiveContract(1L)
            }
            assertTrue(ex.message!!.contains("brak aktywnej rezerwacji", ignoreCase = true))
        }
    }

    @Nested
    inner class EnsureNoOverdue {
        @Test
        fun `passes when balance is zero or positive`() = runTest {
            coEvery { listingService.clientOverdue(any(), any(), any()) } returns BigDecimal("10.00")
            coEvery { contractService.getByClientId(1L, onlyActive = true) } returns emptyList()
            coEvery { invoiceRepo.getLastInvoiceForClient(1L) } returns null
            coEvery { billRepo.getLastBillForClient(1L) } returns null
            service.ensureNoOverdue(1L)
        }

        @Test
        fun `passes when debt is within active contracts sum`() = runTest {
            // 100 net * 1.23 = 123.00 gross threshold; debt 100 is acceptable
            coEvery { listingService.clientOverdue(any(), any(), any()) } returns BigDecimal("-100.00")
            coEvery { contractService.getByClientId(1L, onlyActive = true) } returns listOf(
                Contract(id = 1L, netPrice = BigDecimal("100.00"), vatRate = BigDecimal("23")),
            )
            service.ensureNoOverdue(1L)
        }

        @Test
        fun `throws when debt exceeds active contracts sum`() = runTest {
            coEvery { listingService.clientOverdue(any(), any(), any()) } returns BigDecimal("-200.00")
            coEvery { contractService.getByClientId(1L, onlyActive = true) } returns listOf(
                Contract(id = 1L, netPrice = BigDecimal("100.00"), vatRate = BigDecimal("23")),
            )
            val ex = assertThrows<GateAccessDeniedException> {
                service.ensureNoOverdue(1L)
            }
            assertTrue(ex.message!!.contains("zadłużenie", ignoreCase = true))
        }

        @Test
        fun `uses last invoice amount when no active contracts`() = runTest {
            coEvery { listingService.clientOverdue(any(), any(), any()) } returns BigDecimal("-50.00")
            coEvery { contractService.getByClientId(1L, onlyActive = true) } returns emptyList()
            coEvery { invoiceRepo.getLastInvoiceForClient(1L) } returns Invoice(
                invoiceDate = LocalDate(2026, 6, 1),
                priceWithVatSum = "100.00",
            )
            coEvery { billRepo.getLastBillForClient(1L) } returns null
            service.ensureNoOverdue(1L)
        }

        @Test
        fun `throws when debt exceeds last invoice and no contracts`() = runTest {
            coEvery { listingService.clientOverdue(any(), any(), any()) } returns BigDecimal("-150.00")
            coEvery { contractService.getByClientId(1L, onlyActive = true) } returns emptyList()
            coEvery { invoiceRepo.getLastInvoiceForClient(1L) } returns Invoice(
                invoiceDate = LocalDate(2026, 6, 1),
                priceWithVatSum = "100.00",
            )
            coEvery { billRepo.getLastBillForClient(1L) } returns null
            val ex = assertThrows<GateAccessDeniedException> {
                service.ensureNoOverdue(1L)
            }
            assertTrue(ex.message!!.contains("zadłużenie", ignoreCase = true))
        }

        @Test
        fun `throws when balance is negative and no threshold available`() = runTest {
            coEvery { listingService.clientOverdue(any(), any(), any()) } returns BigDecimal("-5.00")
            coEvery { contractService.getByClientId(1L, onlyActive = true) } returns emptyList()
            coEvery { invoiceRepo.getLastInvoiceForClient(1L) } returns null
            coEvery { billRepo.getLastBillForClient(1L) } returns null
            val ex = assertThrows<GateAccessDeniedException> {
                service.ensureNoOverdue(1L)
            }
            assertTrue(ex.message!!.contains("zadłużenie", ignoreCase = true))
        }
    }

    @Nested
    inner class EnsureCooldown {
        @Test
        fun `passes when never opened`() = runTest {
            coEvery { gateEventRepo.getLastOpenEventEpochMs(1L) } returns null
            service.ensureCooldown(1L)
        }

        @Test
        fun `throws when within cooldown window`() = runTest {
            val now = Clock.System.now().toEpochMilliseconds()
            coEvery { gateEventRepo.getLastOpenEventEpochMs(1L) } returns now - 5_000
            val ex = assertThrows<GateAccessDeniedException> {
                service.ensureCooldown(1L)
            }
            assertTrue(ex.message!!.contains("poczekaj", ignoreCase = true))
        }

        @Test
        fun `passes when cooldown elapsed`() = runTest {
            val now = Clock.System.now().toEpochMilliseconds()
            coEvery { gateEventRepo.getLastOpenEventEpochMs(1L) } returns now - 61_000
            service.ensureCooldown(1L)
        }
    }

    @Nested
    inner class OpenAndLog {
        @Test
        fun `openGate succeeds in mock mode`() = runTest {
            val response = service.openGate()
            assertTrue(response.success)
            assertEquals("Brama została otwarta", response.message)
        }

        @Test
        fun `openGate rejects mangled GATE_REQUEST_BODY before calling SUPLA`() = runTest {
            coEvery { suplaTokenProvider.getAccessToken(any()) } returns "token"
            val broken = GateServiceImpl(
                gateConfig = GateConfig(
                    openUrl = "https://svr111.supla.org/api/channels/8656",
                    method = "PATCH",
                    accessToken = "token",
                    requestBody = "{action:OPEN_CLOSE}",
                    mockMode = false,
                ),
                contractService = contractService,
                listingService = listingService,
                invoiceRepo = invoiceRepo,
                billRepo = billRepo,
                gateEventRepo = gateEventRepo,
                suplaTokenProvider = suplaTokenProvider,
            )
            val ex = assertThrows<IllegalStateException> {
                broken.openGate()
            }
            assertTrue(ex.message!!.contains("GATE_REQUEST_BODY", ignoreCase = true))
            assertTrue(ex.message!!.contains("valid JSON", ignoreCase = true))
        }

        @Test
        fun `logOpenEvent delegates to repo`() = runTest {
            coEvery { gateEventRepo.logOpenEvent(1L, "yard") } returns Unit
            service.logOpenEvent(1L)
            coVerify { gateEventRepo.logOpenEvent(1L, "yard") }
        }
    }
}
