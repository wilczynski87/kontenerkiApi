package com.kontenery.p24.service

import com.kontenery.P24Config
import com.kontenery.data.Client
import com.kontenery.data.ClientPersonalData
import com.kontenery.data.Payment
import com.kontenery.data.PaymentDto
import com.kontenery.data.p24.P24Transaction
import com.kontenery.p24.client.P24ApiClient
import com.kontenery.p24.dto.P24CreateTransactionRequest
import com.kontenery.p24.dto.P24NotificationPayload
import com.kontenery.p24.dto.P24TransactionStatus
import com.kontenery.p24.exception.P24Exception
import com.kontenery.p24.service.impl.P24ServiceImpl
import com.kontenery.repository.P24TransactionRepo
import com.kontenery.service.ClientService
import com.kontenery.service.PaymentService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class P24ServiceImplTest {

    private val config = P24Config(
        environment = "SANDBOX",
        baseUrl = "https://sandbox.przelewy24.pl/api/v1",
        paymentUrl = "https://sandbox.przelewy24.pl/trnRequest",
        merchantId = 12345,
        posId = 12345,
        crc = "crc-secret",
        apiKey = "api-key",
        urlStatus = "https://api.example.com/p24/notification",
        mockMode = true,
    )

    @Test
    fun `toGrosze rounds half up`() {
        assertEquals(1099, P24ServiceImpl.toGrosze(10.99))
        assertEquals(1234, P24ServiceImpl.toGrosze(12.34))
        assertEquals(1, P24ServiceImpl.toGrosze(0.01))
        assertEquals(0, P24ServiceImpl.toGrosze(0.001))
        assertEquals(1, P24ServiceImpl.toGrosze(0.005))
    }

    @Test
    fun `createTransaction in mock mode returns redirect url`() = runBlocking {
        val clientService = mockk<ClientService>()
        coEvery { clientService.findClientById(7L) } returns Client(
            id = 7L,
            clientPrivate = ClientPersonalData(email = "client@example.com"),
        )

        val saved = slot<P24Transaction>()
        val transactionRepo = mockk<P24TransactionRepo>()
        coEvery { transactionRepo.create(capture(saved)) } answers { saved.captured }
        coEvery {
            transactionRepo.updateStatus(any(), any(), any(), any(), any(), any(), any(), any())
        } returns null

        val apiClient = mockk<P24ApiClient>()
        coEvery { apiClient.redirectUrl(any()) } answers {
            "https://sandbox.przelewy24.pl/trnRequest/${firstArg<String>()}"
        }

        val paymentService = mockk<PaymentService>(relaxed = true)
        val service = P24ServiceImpl(config, apiClient, transactionRepo, clientService, paymentService)

        val response = service.createTransaction(
            P24CreateTransactionRequest(
                clientId = 7L,
                amount = 12.34,
                urlReturn = "https://magazynki.example.com/return",
                invoiceNumbers = listOf("FV/1/2026"),
            ),
        )

        assertTrue(response.mock)
        assertEquals(1234, response.amountGrosze)
        assertEquals(P24TransactionStatus.REGISTERED, response.status)
        assertTrue(response.redirectUrl.contains(response.token))
        assertEquals(listOf("FV/1/2026"), saved.captured.invoiceNumbers)
    }

    @Test
    fun `handleNotification creates ledger payment`() = runBlocking {
        val transactionRepo = mockk<P24TransactionRepo>()
        coEvery { transactionRepo.findBySessionId("sess-1") } returnsMany listOf(
            P24Transaction(
                id = 1L,
                sessionId = "sess-1",
                clientId = 7L,
                amountGrosze = 2500,
                currency = "PLN",
                email = "client@example.com",
                invoiceNumbers = listOf("FV/1/2026"),
                urlReturn = "https://return",
                status = P24TransactionStatus.REGISTERED,
                description = "Test payment",
            ),
            P24Transaction(
                id = 1L,
                sessionId = "sess-1",
                clientId = 7L,
                amountGrosze = 2500,
                currency = "PLN",
                email = "client@example.com",
                invoiceNumbers = listOf("FV/1/2026"),
                urlReturn = "https://return",
                status = P24TransactionStatus.NOTIFIED,
                description = "Test payment",
            ),
        )
        coEvery {
            transactionRepo.updateStatus(any(), any(), any(), any(), any(), any(), any(), any())
        } returns null

        val paymentDto = slot<PaymentDto>()
        val paymentService = mockk<PaymentService>()
        coEvery { paymentService.isDuplicated(any()) } returns false
        coEvery { paymentService.createPayment(capture(paymentDto)) } returns Payment(
            id = 99L,
            amount = BigDecimal("25.00"),
            date = LocalDate(2026, 8, 3),
            referenceNumber = "P24-sess-1",
        )

        val service = P24ServiceImpl(
            config = config,
            apiClient = mockk(relaxed = true),
            transactionRepo = transactionRepo,
            clientService = mockk(relaxed = true),
            paymentService = paymentService,
        )

        val ok = service.handleNotification(
            P24NotificationPayload(
                merchantId = 12345,
                posId = 12345,
                sessionId = "sess-1",
                amount = 2500,
                originAmount = 2500,
                currency = "PLN",
                orderId = 555L,
                methodId = 25,
                statement = "p24",
                sign = "ignored-in-mock",
            ),
        )

        assertTrue(ok)
        assertEquals(25.0, paymentDto.captured.amount)
        assertEquals(7L, paymentDto.captured.fromClientId)
        assertEquals("przelewy24", paymentDto.captured.method)
        assertEquals(listOf("FV/1/2026"), paymentDto.captured.forInvoices)
        assertEquals("P24-sess-1", paymentDto.captured.referenceNumber)
        coVerify {
            transactionRepo.updateStatus(
                sessionId = "sess-1",
                status = P24TransactionStatus.VERIFIED,
                orderId = 555L,
                methodId = 25,
                statement = "p24",
                paymentId = 99L,
            )
        }
    }

    @Test
    fun `createTransaction rejects missing client`() {
        val clientService = mockk<ClientService>()
        coEvery { clientService.findClientById(1L) } returns null
        val service = P24ServiceImpl(
            config = config,
            apiClient = mockk(relaxed = true),
            transactionRepo = mockk(relaxed = true),
            clientService = clientService,
            paymentService = mockk(relaxed = true),
        )
        val ex = assertThrows(P24Exception::class.java) {
            runBlocking {
                service.createTransaction(
                    P24CreateTransactionRequest(
                        clientId = 1L,
                        amount = 10.0,
                        urlReturn = "https://return",
                        email = "a@b.pl",
                    ),
                )
            }
        }
        assertEquals(404, ex.statusCode)
    }
}
