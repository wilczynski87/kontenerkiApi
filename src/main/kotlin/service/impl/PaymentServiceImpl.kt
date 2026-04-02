package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.Payment
import com.kontenery.data.PaymentDto
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.utils.SellerAccount
import com.kontenery.data.utils.errors.PaymentError
import com.kontenery.data.utils.errors.ValidationErrorType
import com.kontenery.repository.PaymentRepo
import com.kontenery.service.ClientService
import com.kontenery.service.InvoiceService
import com.kontenery.service.PaymentService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

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
    override suspend fun readPaymentsFromStatement(): List<Payment> {
        TODO("Not yet implemented")
    }
    override suspend fun clientOverdue(clientId: Long, from: LocalDate, to: LocalDate): Double {
        TODO("Not yet implemented")
    }
    override suspend fun validatePayment(
        newPayment: Payment,
        errors: MutableList<PaymentError>?
    ): Boolean {
        // it have to return TRUE if there is no error
//        println("newPayment.referenceNumber: ${newPayment.referenceNumber}, ${newPayment.fromClient?.getName()} ${newPayment.date} ${newPayment.amount}")
        if(newPayment.referenceNumber.isNullOrBlank()) {
            val isDuplicated: Boolean = paymentRepo.isDuplicate(newPayment)
            if(isDuplicated) {
                errors?.add(
                    PaymentError(
                        ValidationErrorType.DUPLICATED.name,
                        "Payment with same parameters already exists",
                        newPayment
                    )
                )
                return false
            }
            // if payment in db with given db -> return null because we do not want duplicates
        } else if(paymentRepo.isPaymentWithReferenceNr(newPayment.referenceNumber!!)) {
            errors?.add(
                PaymentError(
                    ValidationErrorType.DUPLICATED.name,
                    "Payment with REFERENCE nr already exists",
                    newPayment
                )
            )
            return false
        }
        return true
    }

    override suspend fun validatePaymentByParams(
        newPayment: Payment,
        errors: MutableList<PaymentError>?
    ): Boolean {
        // give false when find similar in DB
        if(paymentRepo.isDuplicate(newPayment)) {
            println("payment error: $newPayment")
            return false
        }
        println("Z płatnością ok od: ${newPayment.fromClient?.getName()}")
        return true
    }


    private suspend fun dtoToPayment(dto: PaymentDto): Payment {
        val client: Client? = dto.fromClientId?.let { clientService.findClientById(it) }
        println("client for payment: $client")
        assert(
            client != null
            || dto.amount != null
            || dto.date != null
        )

        val toAccount: SellerAccount? = if(dto.toAccount != null) SellerAccount.fromAccountNumber(dto.toAccount!!)
            else SellerAccount.PRIVATE

        return Payment(
            amount = runCatching { dto.amount?.toBigDecimal() }.getOrNull() ?: BigDecimal.ZERO,
            date = dto.date!!,
            fromClient = client,
            method = dto.method,
            toAccount = toAccount,
            fromAccount = dto.fromAccount,
            title = dto.title,
            forInvoices = forInvoicesMapper(dto.forInvoices!!),
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