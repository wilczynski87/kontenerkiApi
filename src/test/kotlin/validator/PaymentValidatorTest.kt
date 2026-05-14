package com.kontenery.validator

import com.kontenery.data.Payment
import com.kontenery.data.utils.errors.PaymentError
import com.kontenery.data.utils.errors.ValidationErrorType
import com.kontenery.repository.PaymentRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PaymentValidatorTest {

    private lateinit var paymentRepo: PaymentRepo
    private lateinit var validator: PaymentValidator
    private lateinit var errors: MutableList<PaymentError>

    @BeforeEach
    fun setUp() {
        paymentRepo = mockk()
        validator = PaymentValidator(paymentRepo)
        errors = mutableListOf()
    }

    private fun createPayment(
        amount: BigDecimal = BigDecimal("500"),
        date: LocalDate = LocalDate(2026, 4, 15),
        title: String? = "Faktura 1/4/2026",
        referenceNumber: String? = null,
        fromAccount: String? = "72114020040000320278657853"
    ) = Payment(
        amount = amount,
        date = date,
        title = title,
        referenceNumber = referenceNumber,
        fromAccount = fromAccount
    )

    @Nested
    inner class ValidatePayment {

        @Test
        fun `should return true when payment without referenceNumber is not a duplicate`() = runTest {
            val payment = createPayment(referenceNumber = null)
            coEvery { paymentRepo.isDuplicate(payment) } returns false

            val result = validator.validatePayment(payment, errors)

            assertTrue(result)
            assertTrue(errors.isEmpty())
            coVerify { paymentRepo.isDuplicate(payment) }
        }

        @Test
        fun `should return false and add error when payment without referenceNumber is a duplicate`() = runTest {
            val payment = createPayment(referenceNumber = null)
            coEvery { paymentRepo.isDuplicate(payment) } returns true

            val result = validator.validatePayment(payment, errors)

            assertFalse(result)
            assertEquals(1, errors.size)
            assertEquals(ValidationErrorType.DUPLICATED.name, errors[0].title)
            assertEquals("Payment with same parameters already exists", errors[0].message)
            assertSame(payment, errors[0].payment)
        }

        @Test
        fun `should return true when payment with referenceNumber does not exist in db`() = runTest {
            val payment = createPayment(referenceNumber = "REF-2026-001")
            coEvery { paymentRepo.isPaymentWithReferenceNr("REF-2026-001") } returns false

            val result = validator.validatePayment(payment, errors)

            assertTrue(result)
            assertTrue(errors.isEmpty())
            coVerify { paymentRepo.isPaymentWithReferenceNr("REF-2026-001") }
        }

        @Test
        fun `should return false and add error when payment with referenceNumber already exists`() = runTest {
            val payment = createPayment(referenceNumber = "REF-2026-001")
            coEvery { paymentRepo.isPaymentWithReferenceNr("REF-2026-001") } returns true

            val result = validator.validatePayment(payment, errors)

            assertFalse(result)
            assertEquals(1, errors.size)
            assertEquals(ValidationErrorType.DUPLICATED.name, errors[0].title)
            assertEquals("Payment with REFERENCE nr already exists", errors[0].message)
            assertSame(payment, errors[0].payment)
        }

        @Test
        fun `should treat blank referenceNumber same as null - check by isDuplicate`() = runTest {
            val payment = createPayment(referenceNumber = "   ")
            coEvery { paymentRepo.isDuplicate(payment) } returns false

            val result = validator.validatePayment(payment, errors)

            assertTrue(result)
            coVerify { paymentRepo.isDuplicate(payment) }
            coVerify(exactly = 0) { paymentRepo.isPaymentWithReferenceNr(any()) }
        }

        @Test
        fun `should treat empty referenceNumber same as null - check by isDuplicate`() = runTest {
            val payment = createPayment(referenceNumber = "")
            coEvery { paymentRepo.isDuplicate(payment) } returns true

            val result = validator.validatePayment(payment, errors)

            assertFalse(result)
            assertEquals(1, errors.size)
            coVerify { paymentRepo.isDuplicate(payment) }
            coVerify(exactly = 0) { paymentRepo.isPaymentWithReferenceNr(any()) }
        }

        @Test
        fun `should accumulate errors across multiple calls`() = runTest {
            val payment1 = createPayment(amount = BigDecimal("100"), referenceNumber = null)
            val payment2 = createPayment(amount = BigDecimal("200"), referenceNumber = "REF-DUP")
            coEvery { paymentRepo.isDuplicate(payment1) } returns true
            coEvery { paymentRepo.isPaymentWithReferenceNr("REF-DUP") } returns true

            validator.validatePayment(payment1, errors)
            validator.validatePayment(payment2, errors)

            assertEquals(2, errors.size)
            assertEquals(BigDecimal("100"), errors[0].payment?.amount)
            assertEquals(BigDecimal("200"), errors[1].payment?.amount)
        }
    }

    @Nested
    inner class ValidatePaymentByParams {

        @Test
        fun `should return true when payment is not a duplicate`() = runTest {
            val payment = createPayment()
            coEvery { paymentRepo.isDuplicate(payment) } returns false

            val result = validator.validatePaymentByParams(payment)

            assertTrue(result)
        }

        @Test
        fun `should return false when payment is a duplicate`() = runTest {
            val payment = createPayment()
            coEvery { paymentRepo.isDuplicate(payment) } returns true

            val result = validator.validatePaymentByParams(payment)

            assertFalse(result)
        }
    }
}
