package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.ClientOnList
import com.kontenery.data.ClientOnListForFinance
import com.kontenery.data.Payment
import com.kontenery.data.PaymentForFinanceTable
import com.kontenery.data.Product
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.utils.endOfCurrentYear
import com.kontenery.data.utils.now
import com.kontenery.data.utils.startOfCurrentYear
import com.kontenery.data.PaymentsListForFinanceTable
import com.kontenery.repository.*
import com.kontenery.service.ListingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import java.math.BigDecimal
import java.math.RoundingMode

class ListingServiceImpl(
    private val clientsRepo: ClientRepo,
    private val productsRepo: ProductRepo,
    private val contractsRepo: ContractRepo,
    private val paymentsRepo: PaymentRepo,
    private val invoicesRepo: InvoiceRepo,
    private val billRepo: BillRepo,
): ListingService {
    override suspend fun clientsList(page: Int, size: Int): List<ClientOnList> {
        val clients: List<Client> = clientsRepo.getAllClients(page, size)
        return clients.mapNotNull { clientToClientOnList(it) }
    }

    override suspend fun clientsListSize(): Long {
        return clientsRepo.clientsListSize()
    }

    private suspend fun clientToClientOnList(client: Client): ClientOnList? {
        if(client.id == null) throw NullPointerException("Client dose not have ID: $client")
        val from: LocalDate = LocalDate.startOfCurrentYear()
        val to: LocalDate = LocalDate.endOfCurrentYear()
        return try {
            ClientOnList(
                id = client.id,
                name = client.getName(),
                paymentsOverdue = clientOverdue(client.id, from, to),
                contracts = getContractsIdForClient(client.id, true),
                active = client.isActive ?: false,
                invoice = client.needInvoice(),
                lastBill = clientLastInvoiceSend(client),
                clientType = if(client.clientCompany == null) ClientOnList.ClientType.INDIVIDUAL else ClientOnList.ClientType.COMPANY
            )
        } catch (e: Exception) {
            println("clientToClientOnList: $e")
            null
        }
    }

    private suspend fun getContractsIdForClient(id: Long, onlyActive: Boolean = true): List<String> {
        return contractsRepo.findByClientId(id, onlyActive).mapNotNull { it.product?.name }
    }

    override suspend fun clientOverdue(clientId: Long, from: LocalDate, to: LocalDate): BigDecimal? {
        return try {
            coroutineScope {
                val payments = async { paymentsRepo.getPaymentsByClient(0, 1000, clientId, from, to) }
                val invoices = async { invoicesRepo.getInvoicesForClient(0, 1000, clientId, from, to) }
                val bills = async { billRepo.getBillsForClient(0, 1000, clientId, from, to) }

                val paymentSum = payments.await().sumOf { it.amount }
                val billsSum = bills.await().sumOf { it.priceSum?.toBigDecimal() ?: BigDecimal.ZERO }
                val invoiceSum = invoices.await().sumOf { it.priceWithVatSum?.toBigDecimal() ?: BigDecimal.ZERO }

                paymentSum - invoiceSum.setScale(2, RoundingMode.UP) - billsSum.setScale(2, RoundingMode.UP)
            }
        } catch (e: Exception) {
            println("clientOverdue: $e")
            null
        }
    }

    override suspend fun clientsOverdue(
        from: LocalDate,
        to: LocalDate
    ): Map<Long, Double?> {
        val clients: List<Long> = clientsRepo.getAllClients(0, 1000).mapNotNull { it.id }

        val clientsBalance: Map<Long, Double?> = coroutineScope {
            clients.map {
                async { it to clientOverdue(it, from, to)?.toDouble() }
            }.awaitAll()
            .toMap()
        }

        return clientsBalance
    }

    private suspend fun clientLastInvoiceSend(client: Client): LocalDate? {
        val lastInvoice: Invoice? =
            if(client.needInvoice()) invoicesRepo.getLastInvoiceForClient(client.id!!)
            else billRepo.getLastBillForClient(client.id!!)
//        if (lastInvoice != null) {
//            println("${client.getName()}, need invoice: ${client.needInvoice()}, lastInvoice: ${lastInvoice.invoiceNumber}")
//        } else println("${client.getName()}, need invoice: ${client.needInvoice()}, lastInvoice: NIE MA!")

        return lastInvoice?.invoiceSendToClient
    }

    override suspend fun productList(page: Int, size: Int): List<Product> {
        return productsRepo.getAllProduct(page, size)
    }

    override suspend fun clientsFinancesList(
        page: Int,
        size: Int,
        from: LocalDate,
        to: LocalDate,
    ): List<PaymentsListForFinanceTable> = coroutineScope {

        val clients = withContext(Dispatchers.IO) {
            clientsRepo.getAllClients(page, size)
        }

        clients.map {
            async {
                val paymentsList: List<Payment> = paymentsRepo.getPaymentsByClient(0, 1000, it.id!!, from, to)

                PaymentsListForFinanceTable(
                    client = ClientOnListForFinance(
                        clientId = it.id,
                        name = it.getName(),
                        isActive = it.isActive
                    ),
                    payments = addEmptyMonths(paymentsList, to)
                )
            }
        }.awaitAll()
    }
}

private fun addEmptyMonths(payments: List<Payment>, to: LocalDate = LocalDate.now()): List<PaymentForFinanceTable> {
    val paymentForFinanceList: MutableList<PaymentForFinanceTable> = mutableListOf()

    // jeśli zakres nie dotyczy bierzącego roku, to pokaż pełen zakres (za 12 miesięcy)
    val currentMonth: Int = if(to.year != LocalDate.now().year) 12 else LocalDate.now().monthNumber

//    if(payments.isNotEmpty() && payments[0].fromClient?.id == 1L) payments.forEach { println("payment: $it") }

    for (i in 1.. currentMonth) {
        val monthPayments = payments.filter { it.date.monthNumber == i }
        if(monthPayments.isEmpty()) {
            paymentForFinanceList.add(
                PaymentForFinanceTable(
                    null,
                    LocalDate(to.year, i, 1).toString(),
                    0.00,
                )
            )
        } else {
            monthPayments.forEach { monthPayment ->
                paymentForFinanceList.add(
                    PaymentForFinanceTable(
                        monthPayment.id,
                        monthPayment.date.toString(),
                        monthPayment.amount.toDouble(),
                    )
                )
            }
        }
    }

    return paymentForFinanceList
}