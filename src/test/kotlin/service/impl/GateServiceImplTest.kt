package com.kontenery.service.impl

import com.kontenery.GateConfig
import com.kontenery.data.Contract
import com.kontenery.repository.GateEventRepo
import com.kontenery.service.ContractService
import com.kontenery.service.GateAccessDeniedException
import com.kontenery.service.ListingService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
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
    private lateinit var gateEventRepo: GateEventRepo
    private lateinit var service: GateServiceImpl

    @BeforeEach
    fun setUp() {
        contractService = mockk()
        listingService = mockk()
        gateEventRepo = mockk(relaxUnitFun = true)
        service = GateServiceImpl(
            gateConfig = GateConfig(
                openUrl = "",
                mockMode = true,
                cooldownSeconds = 60,
            ),
            contractService = contractService,
            listingService = listingService,
            gateEventRepo = gateEventRepo,
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
        fun `throws when userId is zero (admin placeholder)`() {
            val ex = assertThrows<GateAccessDeniedException> {
                service.checkUserAuthenticated("0")
            }
            assertTrue(ex.message!!.contains("zalogowany", ignoreCase = true))
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
            service.ensureNoOverdue(1L)
        }

        @Test
        fun `throws when balance is negative`() = runTest {
            coEvery { listingService.clientOverdue(any(), any(), any()) } returns BigDecimal("-5.00")
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
        fun `logOpenEvent delegates to repo`() = runTest {
            coEvery { gateEventRepo.logOpenEvent(1L, "yard") } returns Unit
            service.logOpenEvent(1L)
            coVerify { gateEventRepo.logOpenEvent(1L, "yard") }
        }
    }
}
