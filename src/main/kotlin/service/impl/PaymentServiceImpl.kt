package com.kontenery.service.impl

import com.kontenery.library.model.Client
import com.kontenery.library.model.Payment
import com.kontenery.library.model.PaymentDto
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.SellerAccount
import com.kontenery.library.utils.startOfCurrentYear
import com.kontenery.repository.PaymentRepo
import com.kontenery.service.ClientService
import com.kontenery.service.InvoiceService
import com.kontenery.service.PaymentService
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import kotlin.math.roundToInt

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
        TODO("Not yet implemented")
    }

    override suspend fun readPaymentsFromStatement(): List<Payment> {
        TODO("Not yet implemented")
    }

    override suspend fun clientOverdue(clientId: Long, from: LocalDate, to: LocalDate): Double {
        TODO("Not yet implemented")
    }


    private suspend fun dtoToPayment(dto: PaymentDto): Payment {
        val client: Client? = dto.fromClientId?.let { clientService.findClientById(it) }

        assert(
            client != null
            || dto.amount != null
            || dto.date != null
        )

        val toAccount: SellerAccount? = if(dto.toAccount != null) SellerAccount.fromAccountNumber(dto.toAccount!!)
            else SellerAccount.PRIVATE

        return Payment(
            amount = dto.amount!!,
            date = dto.date!!,
            fromClient = client,
            method = dto.method,
            toAccount = toAccount,
            fromAccount = dto.fromAccount,
            title = dto.title,
            forInvoices = forInvoicesMapper(dto.forInvoices!!)
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