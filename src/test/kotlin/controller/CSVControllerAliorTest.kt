package com.kontenery.controller

import com.kontenery.data.Client
import com.kontenery.data.Payment
import com.kontenery.data.PaymentDto
import com.kontenery.data.payment.PaymentsRecogniseList
import com.kontenery.data.utils.errors.PaymentError
import com.kontenery.data.utils.errors.ValidationErrorType
import com.kontenery.service.CSVService
import com.kontenery.service.PaymentService
import com.kontenery.validator.PaymentValidator
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CSVControllerAliorTest {

    private val client = Client(id = 1L, clientCompany = null, clientPrivate = null)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun payment(
        amount: BigDecimal = BigDecimal("500"),
        fromClient: Client? = client,
        referenceNumber: String? = null,
        title: String = "Faktura 1/4/2026",
    ) = Payment(
        amount = amount,
        date = LocalDate(2026, 4, 15),
        fromClient = fromClient,
        title = title,
        referenceNumber = referenceNumber,
        fromAccount = "72114020040000320278657853",
    )

    private suspend fun postAlior(
        csvService: CSVService,
        paymentService: PaymentService,
        paymentValidator: PaymentValidator,
        payments: List<Payment>,
    ): PaymentsRecogniseList {
        coEvery { csvService.readCSVAlior(any()) } returns payments
        lateinit var result: PaymentsRecogniseList
        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    CSVController(csvService, paymentService, paymentValidator)
                }
            }
            val httpClient = createClient { }
            val response = httpClient.post("/csv/Alior") {
                contentType(ContentType.Application.Json)
                setBody("""{"message":"csv-data"}""")
            }
            result = json.decodeFromString(response.bodyAsText())
        }
        return result
    }

    @Test
    fun `categorizes unrecognized positive payments`() = runTest {
        val unrecognized = payment(fromClient = null, title = "Nieznany przelew")
        val csvService = mockk<CSVService>()
        val paymentService = mockk<PaymentService>(relaxed = true)
        val paymentValidator = mockk<PaymentValidator>()
        coEvery { paymentValidator.validatePaymentByParams(unrecognized) } returns true

        val result = postAlior(csvService, paymentService, paymentValidator, listOf(unrecognized))

        assertEquals(1, result.unrecognizedPayments?.size)
        assertEquals("Nieznany przelew", result.unrecognizedPayments?.single()?.title)
        assertTrue(result.oldPayments.isNullOrEmpty())
        assertTrue(result.newPayments.isNullOrEmpty())
        coVerify(exactly = 0) { paymentService.createPayment(any()) }
    }

    @Test
    fun `skips zero amount unrecognized payments`() = runTest {
        val zeroPayment = payment(fromClient = null, amount = BigDecimal.ZERO)
        val csvService = mockk<CSVService>()
        val paymentService = mockk<PaymentService>(relaxed = true)
        val paymentValidator = mockk<PaymentValidator>()
        coEvery { paymentValidator.validatePaymentByParams(zeroPayment) } returns true

        val result = postAlior(csvService, paymentService, paymentValidator, listOf(zeroPayment))

        assertTrue(result.unrecognizedPayments.isNullOrEmpty())
    }

    @Test
    fun `categorizes duplicate payments with reference number`() = runTest {
        val duplicate = payment(referenceNumber = "REF-DUP")
        val csvService = mockk<CSVService>()
        val paymentService = mockk<PaymentService>()
        val paymentValidator = mockk<PaymentValidator>()
        coEvery { paymentValidator.validatePaymentByParams(duplicate) } returns true
        coEvery { paymentService.isDuplicated(duplicate) } returns true
        coEvery { paymentValidator.validatePayment(duplicate, any()) } returns false

        val result = postAlior(csvService, paymentService, paymentValidator, listOf(duplicate))

        assertEquals(1, result.oldPayments?.size)
        assertEquals("REF-DUP", result.oldPayments?.single()?.referenceNumber)
        assertTrue(result.newPayments.isNullOrEmpty())
        coVerify(exactly = 0) { paymentService.createPayment(any()) }
    }

    @Test
    fun `saves valid payment and returns it in newPayments`() = runTest {
        val newPayment = payment(referenceNumber = "REF-NEW")
        val paymentDto = newPayment.toDto()
        val csvService = mockk<CSVService>()
        val paymentService = mockk<PaymentService>()
        val paymentValidator = mockk<PaymentValidator>()
        coEvery { paymentValidator.validatePaymentByParams(newPayment) } returns true
        coEvery { paymentService.isDuplicated(newPayment) } returns false
        coEvery { paymentValidator.validatePayment(newPayment, any()) } returns true
        coEvery { paymentService.createPayment(paymentDto) } returns newPayment

        val result = postAlior(csvService, paymentService, paymentValidator, listOf(newPayment))

        assertEquals(1, result.newPayments?.size)
        assertEquals("REF-NEW", result.newPayments?.single()?.referenceNumber)
        assertTrue(result.oldPayments.isNullOrEmpty())
        coVerify { paymentService.createPayment(paymentDto) }
    }

    @Test
    fun `excludes param duplicates filtered by validatePaymentByParams`() = runTest {
        val paramDuplicate = payment(referenceNumber = null)
        val csvService = mockk<CSVService>()
        val paymentService = mockk<PaymentService>(relaxed = true)
        val paymentValidator = mockk<PaymentValidator>()
        coEvery { paymentValidator.validatePaymentByParams(paramDuplicate) } returns false

        val result = postAlior(csvService, paymentService, paymentValidator, listOf(paramDuplicate))

        assertTrue(result.unrecognizedPayments.isNullOrEmpty())
        assertTrue(result.oldPayments.isNullOrEmpty())
        assertTrue(result.newPayments.isNullOrEmpty())
        coVerify(exactly = 0) { paymentService.isDuplicated(any()) }
        coVerify(exactly = 0) { paymentService.createPayment(any()) }
    }

    @Test
    fun `includes validation errors in response`() = runTest {
        val invalid = payment(referenceNumber = "REF-INVALID")
        val csvService = mockk<CSVService>()
        val paymentService = mockk<PaymentService>()
        val paymentValidator = mockk<PaymentValidator>()
        coEvery { paymentValidator.validatePaymentByParams(invalid) } returns true
        coEvery { paymentService.isDuplicated(invalid) } returns false
        coEvery { paymentValidator.validatePayment(invalid, any()) } coAnswers {
            val errors = secondArg<MutableList<PaymentError>>()
            errors.add(
                PaymentError(
                    ValidationErrorType.DUPLICATED.name,
                    "Payment with REFERENCE nr already exists",
                    invalid,
                ),
            )
            false
        }

        val result = postAlior(csvService, paymentService, paymentValidator, listOf(invalid))

        assertEquals(1, result.errors?.size)
        assertEquals(ValidationErrorType.DUPLICATED.name, result.errors?.single()?.title)
        assertTrue(result.newPayments.isNullOrEmpty())
        coVerify(exactly = 0) { paymentService.createPayment(any<PaymentDto>()) }
    }

    @Test
    fun `returns bad request when csv parsing fails`() = runTest {
        val csvService = mockk<CSVService>()
        val paymentService = mockk<PaymentService>()
        val paymentValidator = mockk<PaymentValidator>()
        coEvery { csvService.readCSVAlior(any()) } throws IllegalArgumentException("bad csv")

        testApplication {
            application {
                install(ContentNegotiation) { json(json) }
                routing {
                    CSVController(csvService, paymentService, paymentValidator)
                }
            }
            val httpClient = createClient { }
            val response = httpClient.post("/csv/Alior") {
                contentType(ContentType.Application.Json)
                setBody("""{"message":"broken"}""")
            }
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }
}
