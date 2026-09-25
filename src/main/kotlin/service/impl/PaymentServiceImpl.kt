package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.Payment
import com.kontenery.data.PaymentDto
import com.kontenery.data.PaymentMethod
import com.kontenery.data.PaymentTransferRequest
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.utils.SellerAccount
import com.kontenery.repository.PaymentRepo
import com.kontenery.service.ClientService
import com.kontenery.service.InvoiceService
import com.kontenery.service.PaymentService
import com.kontenery.service.PaymentTransferResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode

class PaymentServiceImpl(
    private val paymentRepo: PaymentRepo,
    private val clientService: ClientService,
    private val invoiceService: InvoiceService,
): PaymentService {
    override suspend fun getPaymentsByClient(
        page: Int,
        size: Int,
        clientId: Long,
        from: LocalDate,
        to: LocalDate
    ): List<Payment> {
        return paymentRepo.getPaymentsByClient(page, size, clientId, from, to)
    }
    override suspend fun createPayment(paymentDto: PaymentDto): Payment {
        val payment: Payment = dtoToPayment(paymentDto)
        return paymentRepo.createPayment(payment)
    }
    override suspend fun updatePayment(paymentDto: PaymentDto): Payment {
        val payment: Payment = dtoToPayment(paymentDto)
        return paymentRepo.updatePayment(payment)
    }
    override suspend fun deletePayment(paymentId: Long): Boolean {
        return paymentRepo.deletePayment(paymentId)
    }

    override suspend fun transferPayment(request: PaymentTransferRequest): PaymentTransferResult {
        val source = paymentRepo.findById(request.sourcePaymentId)
            ?: return PaymentTransferResult.NotFound("Nie znaleziono płatności ${request.sourcePaymentId}")

        val sourceClientId = source.fromClient?.id
            ?: return PaymentTransferResult.BadRequest("Płatność źródłowa nie ma przypisanego klienta")

        if (sourceClientId == request.targetClientId) {
            return PaymentTransferResult.BadRequest("Klient docelowy musi być inny niż źródłowy")
        }

        if (source.amount <= BigDecimal.ZERO) {
            return PaymentTransferResult.BadRequest("Zaliczenie możliwe tylko z płatności o dodatniej kwocie")
        }

        val targetClient = clientService.findClientById(request.targetClientId)
            ?: return PaymentTransferResult.NotFound("Nie znaleziono klienta docelowego ${request.targetClientId}")

        val transferAmount = resolveTransferAmount(request.amount, source.amount)
            ?: return PaymentTransferResult.BadRequest(
                "Kwota zaliczenia musi być większa od 0 i nie większa niż ${source.amount}",
            )

        val transferDate = request.date ?: source.date
        val reference = "ZAL-${source.id}-${System.currentTimeMillis()}"
        val baseTitle = request.title?.trim()?.takeIf { it.isNotEmpty() }
        val debitTitle = baseTitle
            ?: "Zaliczenie na klienta #${targetClient.id} (z płatności #${source.id})"
        val creditTitle = baseTitle
            ?: "Zaliczenie z klienta #$sourceClientId (z płatności #${source.id})"

        val debit = Payment(
            amount = transferAmount.negate(),
            date = transferDate,
            fromClient = source.fromClient,
            method = PaymentMethod.ZALICZENIE.polishName,
            toAccount = source.toAccount,
            title = debitTitle,
            forInvoices = emptyList(),
            referenceNumber = reference,
        )
        val credit = Payment(
            amount = transferAmount,
            date = transferDate,
            fromClient = targetClient,
            method = PaymentMethod.ZALICZENIE.polishName,
            toAccount = source.toAccount,
            title = creditTitle,
            forInvoices = emptyList(),
            referenceNumber = reference,
        )

        val (savedDebit, savedCredit) = paymentRepo.createTransferPair(debit, credit)
        return PaymentTransferResult.Success(savedDebit, savedCredit)
    }

    override suspend fun readPaymentsFromStatement(): List<Payment> {
        TODO("Not yet implemented")
    }
    override suspend fun clientOverdue(clientId: Long, from: LocalDate, to: LocalDate): Double {
        TODO("Not yet implemented")
    }
    override suspend fun isDuplicated(newPayment: Payment): Boolean {
        return if(newPayment.referenceNumber.isNullOrBlank()) {
            paymentRepo.isDuplicate(newPayment)
        } else paymentRepo.isPaymentWithReferenceNr(newPayment.referenceNumber)
    }

    private fun resolveTransferAmount(
        requested: Double?,
        sourceAmount: BigDecimal,
    ): BigDecimal? {
        val amount = when (requested) {
            null -> sourceAmount
            else -> runCatching { requested.toBigDecimal() }.getOrNull() ?: return null
        }.setScale(2, RoundingMode.HALF_UP)

        if (amount <= BigDecimal.ZERO) return null
        if (amount > sourceAmount) return null
        return amount
    }

    private suspend fun dtoToPayment(dto: PaymentDto): Payment {
        val client: Client? = dto.fromClientId?.let { clientService.findClientById(it) }
        println("client for payment: $client")
        assert(
            client != null
            || dto.amount != null
            || dto.date != null
        )

        val toAccount: SellerAccount? = if(dto.toAccount != null) SellerAccount.fromAccountNumber(dto.toAccount)
            else SellerAccount.PRIVATE

        return Payment(
            amount = runCatching { dto.amount?.toBigDecimal() }.getOrNull() ?: BigDecimal.ZERO,
            date = dto.date!!,
            fromClient = client,
            method = dto.method,
            toAccount = toAccount,
            fromAccount = dto.fromAccount,
            title = dto.title,
            forInvoices = forInvoicesMapper(dto.forInvoices ?: emptyList()),
            referenceNumber = dto.referenceNumber
        )
    }

    private suspend fun forInvoicesMapper(invoicesNumber: List<String>): List<Invoice> = coroutineScope {
        invoicesNumber.map { invoiceNumber ->
            async {
                invoiceService.getInvoiceByNumber(invoiceNumber)
            }
        }.awaitAll().filterNotNull()
    }

}
