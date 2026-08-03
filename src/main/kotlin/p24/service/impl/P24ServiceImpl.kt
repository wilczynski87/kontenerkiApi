package com.kontenery.p24.service.impl

import com.kontenery.P24Config
import com.kontenery.data.PaymentDto
import com.kontenery.data.p24.P24Transaction
import com.kontenery.data.utils.now
import com.kontenery.p24.client.P24ApiClient
import com.kontenery.p24.crypto.P24Sign
import com.kontenery.p24.dto.P24CreateTransactionRequest
import com.kontenery.p24.dto.P24CreateTransactionResponse
import com.kontenery.p24.dto.P24NotificationPayload
import com.kontenery.p24.dto.P24RegisterTransactionRequest
import com.kontenery.p24.dto.P24TransactionStatus
import com.kontenery.p24.dto.P24TransactionStatusResponse
import com.kontenery.p24.dto.P24VerifyTransactionRequest
import com.kontenery.p24.exception.P24Exception
import com.kontenery.p24.service.P24Service
import com.kontenery.repository.P24TransactionRepo
import com.kontenery.service.ClientService
import com.kontenery.service.PaymentService
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory

class P24ServiceImpl(
    private val config: P24Config,
    private val apiClient: P24ApiClient,
    private val transactionRepo: P24TransactionRepo,
    private val clientService: ClientService,
    private val paymentService: PaymentService,
) : P24Service {

    private val log = LoggerFactory.getLogger(P24ServiceImpl::class.java)

    override suspend fun createTransaction(request: P24CreateTransactionRequest): P24CreateTransactionResponse {
        validateCreateRequest(request)

        val client = clientService.findClientById(request.clientId)
            ?: throw P24Exception("Client ${request.clientId} not found", statusCode = 404)

        val email = request.email?.trim()?.takeIf { it.isNotEmpty() }
            ?: client.clientCompany?.email?.trim()?.takeIf { it.isNotEmpty() }
            ?: client.clientPrivate?.email?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw P24Exception("Email is required for P24 transaction", statusCode = 400)

        val amountGrosze = toGrosze(request.amount)
        if (amountGrosze <= 0) {
            throw P24Exception("Amount must be greater than zero", statusCode = 400)
        }

        val sessionId = "k-${request.clientId}-${UUID.randomUUID()}"
        val currency = request.currency.uppercase()
        val description = request.description?.trim()?.takeIf { it.isNotEmpty() }
            ?: buildDefaultDescription(request.invoiceNumbers, request.clientId)

        transactionRepo.create(
            P24Transaction(
                sessionId = sessionId,
                clientId = request.clientId,
                amountGrosze = amountGrosze,
                currency = currency,
                description = description,
                email = email,
                invoiceNumbers = request.invoiceNumbers,
                urlReturn = request.urlReturn,
                status = P24TransactionStatus.PENDING,
            ),
        )

        if (config.mockMode) {
            val token = "mock-$sessionId"
            val redirectUrl = apiClient.redirectUrl(token)
            transactionRepo.updateStatus(
                sessionId = sessionId,
                status = P24TransactionStatus.REGISTERED,
                token = token,
            )
            return P24CreateTransactionResponse(
                sessionId = sessionId,
                token = token,
                redirectUrl = redirectUrl,
                amountGrosze = amountGrosze,
                currency = currency,
                status = P24TransactionStatus.REGISTERED,
                mock = true,
            )
        }

        val merchantId = requireMerchantId()
        val posId = requirePosId()
        val crc = requireCrc()
        val urlStatus = config.urlStatus
            ?: throw P24Exception("P24_URL_STATUS is not configured", statusCode = 500)

        val sign = P24Sign.registerSign(
            sessionId = sessionId,
            merchantId = merchantId,
            amount = amountGrosze,
            currency = currency,
            crc = crc,
        )

        try {
            val token = apiClient.registerTransaction(
                P24RegisterTransactionRequest(
                    merchantId = merchantId,
                    posId = posId,
                    sessionId = sessionId,
                    amount = amountGrosze,
                    currency = currency,
                    description = description,
                    email = email,
                    country = request.country,
                    language = request.language,
                    urlReturn = request.urlReturn,
                    urlStatus = urlStatus,
                    sign = sign,
                ),
            )
            val redirectUrl = apiClient.redirectUrl(token)
            transactionRepo.updateStatus(
                sessionId = sessionId,
                status = P24TransactionStatus.REGISTERED,
                token = token,
            )
            return P24CreateTransactionResponse(
                sessionId = sessionId,
                token = token,
                redirectUrl = redirectUrl,
                amountGrosze = amountGrosze,
                currency = currency,
                status = P24TransactionStatus.REGISTERED,
                mock = false,
            )
        } catch (e: Exception) {
            val message = e.message ?: "register failed"
            transactionRepo.updateStatus(
                sessionId = sessionId,
                status = P24TransactionStatus.FAILED,
                errorMessage = message.take(500),
            )
            if (e is P24Exception) throw e
            throw P24Exception("P24 register failed: $message", statusCode = 502, cause = e)
        }
    }

    override suspend fun getTransaction(sessionId: String): P24TransactionStatusResponse {
        val tx = transactionRepo.findBySessionId(sessionId)
            ?: throw P24Exception("Transaction not found: $sessionId", statusCode = 404)
        return P24TransactionStatusResponse(
            sessionId = tx.sessionId,
            status = tx.status,
            orderId = tx.orderId,
            amountGrosze = tx.amountGrosze,
            currency = tx.currency,
            paymentId = tx.paymentId,
            invoiceNumbers = tx.invoiceNumbers,
        )
    }

    override suspend fun handleNotification(payload: P24NotificationPayload): Boolean {
        if (!config.mockMode) {
            val crc = requireCrc()
            val merchantId = requireMerchantId()
            val posId = requirePosId()

            if (payload.merchantId != merchantId || payload.posId != posId) {
                throw P24Exception("Notification merchant/pos mismatch", statusCode = 400)
            }

            val signOk = P24Sign.isValidNotificationSign(
                merchantId = payload.merchantId,
                posId = payload.posId,
                sessionId = payload.sessionId,
                amount = payload.amount,
                originAmount = payload.originAmount,
                currency = payload.currency,
                orderId = payload.orderId,
                methodId = payload.methodId,
                statement = payload.statement,
                crc = crc,
                providedSign = payload.sign,
            )
            if (!signOk) {
                throw P24Exception("Invalid notification signature", statusCode = 400)
            }
        }

        val tx = transactionRepo.findBySessionId(payload.sessionId)
            ?: throw P24Exception("Unknown sessionId: ${payload.sessionId}", statusCode = 404)

        if (tx.status == P24TransactionStatus.VERIFIED && tx.paymentId != null) {
            log.info("P24 notification idempotent replay for sessionId={}", payload.sessionId)
            return true
        }

        if (payload.amount != tx.amountGrosze || payload.currency != tx.currency) {
            transactionRepo.updateStatus(
                sessionId = tx.sessionId,
                status = P24TransactionStatus.FAILED,
                orderId = payload.orderId,
                methodId = payload.methodId,
                statement = payload.statement,
                errorMessage = "Amount/currency mismatch vs registered transaction",
            )
            throw P24Exception("Notification amount/currency mismatch", statusCode = 400)
        }

        transactionRepo.updateStatus(
            sessionId = tx.sessionId,
            status = P24TransactionStatus.NOTIFIED,
            orderId = payload.orderId,
            methodId = payload.methodId,
            statement = payload.statement,
        )

        if (!config.mockMode) {
            val crc = requireCrc()
            val merchantId = requireMerchantId()
            val posId = requirePosId()
            val verifySign = P24Sign.verifySign(
                sessionId = payload.sessionId,
                orderId = payload.orderId,
                amount = payload.amount,
                currency = payload.currency,
                crc = crc,
            )
            apiClient.verifyTransaction(
                P24VerifyTransactionRequest(
                    merchantId = merchantId,
                    posId = posId,
                    sessionId = payload.sessionId,
                    amount = payload.amount,
                    currency = payload.currency,
                    orderId = payload.orderId,
                    sign = verifySign,
                ),
            )
        }

        val referenceNumber = "P24-${payload.sessionId}"
        if (paymentService.isDuplicated(
                com.kontenery.data.Payment(
                    amount = fromGrosze(payload.amount),
                    date = LocalDate.now(),
                    referenceNumber = referenceNumber,
                ),
            )
        ) {
            transactionRepo.updateStatus(
                sessionId = payload.sessionId,
                status = P24TransactionStatus.VERIFIED,
                orderId = payload.orderId,
                methodId = payload.methodId,
                statement = payload.statement,
            )
            log.info("P24 payment already in ledger for sessionId={}", payload.sessionId)
            return true
        }

        val payment = paymentService.createPayment(
            PaymentDto(
                amount = fromGrosze(payload.amount).toDouble(),
                date = LocalDate.now(),
                fromClientId = tx.clientId,
                method = METHOD_PRZELEWY24,
                title = tx.description ?: "Przelewy24 ${payload.sessionId}",
                forInvoices = tx.invoiceNumbers,
                referenceNumber = referenceNumber,
            ),
        )

        transactionRepo.updateStatus(
            sessionId = payload.sessionId,
            status = P24TransactionStatus.VERIFIED,
            orderId = payload.orderId,
            methodId = payload.methodId,
            statement = payload.statement,
            paymentId = payment.id,
        )
        log.info(
            "P24 payment verified sessionId={} orderId={} paymentId={}",
            payload.sessionId,
            payload.orderId,
            payment.id,
        )
        return true
    }

    private fun validateCreateRequest(request: P24CreateTransactionRequest) {
        if (request.clientId <= 0) {
            throw P24Exception("clientId is required", statusCode = 400)
        }
        if (request.urlReturn.isBlank()) {
            throw P24Exception("urlReturn is required", statusCode = 400)
        }
        if (request.currency.isBlank()) {
            throw P24Exception("currency is required", statusCode = 400)
        }
    }

    private fun buildDefaultDescription(invoiceNumbers: List<String>, clientId: Long): String =
        if (invoiceNumbers.isNotEmpty()) {
            "Faktury: ${invoiceNumbers.joinToString(", ")}".take(255)
        } else {
            "Platnosc klienta $clientId"
        }

    private fun requireMerchantId(): Int =
        config.merchantId ?: throw P24Exception("P24_MERCHANT_ID is not configured", statusCode = 500)

    private fun requirePosId(): Int =
        config.posId ?: config.merchantId
            ?: throw P24Exception("P24_POS_ID is not configured", statusCode = 500)

    private fun requireCrc(): String =
        config.crc?.takeIf { it.isNotBlank() }
            ?: throw P24Exception("P24_CRC is not configured", statusCode = 500)

    companion object {
        const val METHOD_PRZELEWY24 = "przelewy24"

        fun toGrosze(amountPln: Double): Int =
            BigDecimal.valueOf(amountPln)
                .multiply(BigDecimal(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact()

        fun fromGrosze(amountGrosze: Int): BigDecimal =
            BigDecimal(amountGrosze).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
    }
}
