package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.Payment
import com.kontenery.data.PaymentMethod
import com.kontenery.data.PaymentTransferRequest
import com.kontenery.repository.PaymentRepo
import com.kontenery.service.ClientService
import com.kontenery.service.InvoiceService
import com.kontenery.service.PaymentTransferResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PaymentServiceImplTest {

    private lateinit var paymentRepo: PaymentRepo
    private lateinit var clientService: ClientService
    private lateinit var invoiceService: InvoiceService
    private lateinit var service: PaymentServiceImpl

    @BeforeEach
    fun setUp() {
        paymentRepo = mockk()
        clientService = mockk()
        invoiceService = mockk()
        service = PaymentServiceImpl(paymentRepo, clientService, invoiceService)
    }

    private fun createPayment(referenceNumber: String? = null) = Payment(
        amount = BigDecimal("500"),
        date = LocalDate(2026, 4, 15),
        title = "Faktura 1/4/2026",
        referenceNumber = referenceNumber,
        fromAccount = "72114020040000320278657853",
    )

    private fun client(id: Long) = Client(id = id)

    @Nested
    inner class IsDuplicated {

        @Test
        fun `returns true when payment without reference is duplicate by params`() = runTest {
            val payment = createPayment(referenceNumber = null)
            coEvery { paymentRepo.isDuplicate(payment) } returns true

            assertTrue(service.isDuplicated(payment))

            coVerify { paymentRepo.isDuplicate(payment) }
            coVerify(exactly = 0) { paymentRepo.isPaymentWithReferenceNr(any()) }
        }

        @Test
        fun `returns false when payment without reference is not duplicate`() = runTest {
            val payment = createPayment(referenceNumber = null)
            coEvery { paymentRepo.isDuplicate(payment) } returns false

            assertFalse(service.isDuplicated(payment))
        }

        @Test
        fun `treats blank reference as missing and checks by params`() = runTest {
            val payment = createPayment(referenceNumber = "  ")
            coEvery { paymentRepo.isDuplicate(payment) } returns true

            assertTrue(service.isDuplicated(payment))

            coVerify { paymentRepo.isDuplicate(payment) }
            coVerify(exactly = 0) { paymentRepo.isPaymentWithReferenceNr(any()) }
        }

        @Test
        fun `returns true when payment with reference already exists`() = runTest {
            val payment = createPayment(referenceNumber = "REF-2026-001")
            coEvery { paymentRepo.isPaymentWithReferenceNr("REF-2026-001") } returns true

            assertTrue(service.isDuplicated(payment))

            coVerify { paymentRepo.isPaymentWithReferenceNr("REF-2026-001") }
            coVerify(exactly = 0) { paymentRepo.isDuplicate(any()) }
        }

        @Test
        fun `returns false when payment with reference does not exist`() = runTest {
            val payment = createPayment(referenceNumber = "REF-2026-001")
            coEvery { paymentRepo.isPaymentWithReferenceNr("REF-2026-001") } returns false

            assertFalse(service.isDuplicated(payment))
        }
    }

    @Nested
    inner class TransferPayment {

        @Test
        fun `creates debit and credit zaliczenie pair`() = runTest {
            val sourceClient = client(1L)
            val targetClient = client(2L)
            val source = Payment(
                id = 10L,
                amount = BigDecimal("200.00"),
                date = LocalDate(2026, 9, 1),
                fromClient = sourceClient,
                title = "Wpłata",
            )
            coEvery { paymentRepo.findById(10L) } returns source
            coEvery { clientService.findClientById(2L) } returns targetClient

            val debitSlot = slot<Payment>()
            val creditSlot = slot<Payment>()
            coEvery {
                paymentRepo.createTransferPair(capture(debitSlot), capture(creditSlot))
            } answers {
                debitSlot.captured.copy(id = 11L) to creditSlot.captured.copy(id = 12L)
            }

            val result = service.transferPayment(
                PaymentTransferRequest(sourcePaymentId = 10L, targetClientId = 2L),
            )

            assertTrue(result is PaymentTransferResult.Success)
            assertEquals(BigDecimal("-200.00"), debitSlot.captured.amount)
            assertEquals(BigDecimal("200.00"), creditSlot.captured.amount)
            assertEquals(PaymentMethod.ZALICZENIE.polishName, debitSlot.captured.method)
            assertEquals(PaymentMethod.ZALICZENIE.polishName, creditSlot.captured.method)
            assertEquals(1L, debitSlot.captured.fromClient?.id)
            assertEquals(2L, creditSlot.captured.fromClient?.id)
            assertEquals(debitSlot.captured.referenceNumber, creditSlot.captured.referenceNumber)
            assertTrue(debitSlot.captured.referenceNumber!!.startsWith("ZAL-10-"))
        }

        @Test
        fun `supports partial amount`() = runTest {
            val source = Payment(
                id = 10L,
                amount = BigDecimal("200.00"),
                date = LocalDate(2026, 9, 1),
                fromClient = client(1L),
            )
            coEvery { paymentRepo.findById(10L) } returns source
            coEvery { clientService.findClientById(2L) } returns client(2L)

            val debitSlot = slot<Payment>()
            val creditSlot = slot<Payment>()
            coEvery {
                paymentRepo.createTransferPair(capture(debitSlot), capture(creditSlot))
            } answers {
                debitSlot.captured to creditSlot.captured
            }

            service.transferPayment(
                PaymentTransferRequest(
                    sourcePaymentId = 10L,
                    targetClientId = 2L,
                    amount = 50.0,
                ),
            )

            assertEquals(BigDecimal("-50.00"), debitSlot.captured.amount)
            assertEquals(BigDecimal("50.00"), creditSlot.captured.amount)
        }

        @Test
        fun `rejects same source and target client`() = runTest {
            coEvery { paymentRepo.findById(10L) } returns Payment(
                id = 10L,
                amount = BigDecimal("100"),
                date = LocalDate(2026, 9, 1),
                fromClient = client(1L),
            )

            val result = service.transferPayment(
                PaymentTransferRequest(sourcePaymentId = 10L, targetClientId = 1L),
            )

            assertTrue(result is PaymentTransferResult.BadRequest)
            coVerify(exactly = 0) { paymentRepo.createTransferPair(any(), any()) }
        }

        @Test
        fun `rejects amount greater than source`() = runTest {
            coEvery { paymentRepo.findById(10L) } returns Payment(
                id = 10L,
                amount = BigDecimal("100"),
                date = LocalDate(2026, 9, 1),
                fromClient = client(1L),
            )
            coEvery { clientService.findClientById(2L) } returns client(2L)

            val result = service.transferPayment(
                PaymentTransferRequest(sourcePaymentId = 10L, targetClientId = 2L, amount = 150.0),
            )

            assertTrue(result is PaymentTransferResult.BadRequest)
        }

        @Test
        fun `returns not found when source payment missing`() = runTest {
            coEvery { paymentRepo.findById(99L) } returns null

            val result = service.transferPayment(
                PaymentTransferRequest(sourcePaymentId = 99L, targetClientId = 2L),
            )

            assertTrue(result is PaymentTransferResult.NotFound)
        }
    }
}
