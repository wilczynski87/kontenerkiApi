package com.kontenery.service.impl

import com.kontenery.data.Payment
import com.kontenery.repository.PaymentRepo
import com.kontenery.service.ClientService
import com.kontenery.service.InvoiceService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
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
}
