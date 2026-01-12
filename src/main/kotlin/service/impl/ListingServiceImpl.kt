package com.kontenery.service.impl

import com.kontenery.library.model.Client
import com.kontenery.library.model.ClientOnList
import com.kontenery.library.model.ClientOnListForFinance
import com.kontenery.library.model.Payment
import com.kontenery.library.model.PaymentForFinanceTable
import com.kontenery.library.model.PaymentsListForFinanceTable
import com.kontenery.library.model.Product
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.endOfCurrentYear
import com.kontenery.library.utils.startOfCurrentYear
import com.kontenery.repository.*
import com.kontenery.service.ListingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
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
                id = client.id!!,
                name = client.getName(),
                paymentsOverdue = clientOverdue(client, from, to),
                contracts = getContractsIdForClient(client.id!!, true),
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

    private suspend fun clientOverdue(client: Client, from: LocalDate, to: LocalDate): BigDecimal? {
        return try {

            val payments: List<Payment> = paymentsRepo.getPaymentsByClient(0, 1000, client.id!!, from, to)
            val invoices: List<Invoice> =
                invoicesRepo.getInvoicesForClient(0, 1000, client.id!!, from, to) + billRepo.getBillsForClient(0, 1000, client.id!!, from, to)
//                if(client.needInvoice()) invoicesRepo.getInvoicesForClient(0, 1000, client.id!!, from, to)
//                else billRepo.getBillsForClient(0, 1000, client.id!!, from, to)

            val paymentSum = payments.sumOf { it.amount }
            val invoiceSum = invoices.sumOf { it.priceWithVatSum?.toBigDecimal() ?: it.priceSum?.toBigDecimal() ?: BigDecimal.ZERO }

//            println("${client.getName()}, need invoice: ${client.needInvoice()}, paymentSum: $paymentSum, invoiceSum: $invoiceSum")

            paymentSum - invoiceSum.setScale(2, RoundingMode.UP)
        } catch (e: Exception) {
            println("clientOverdue: $e")
            null
        }
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
                        it.id,
                        it.getName()
                    ),
                    payments = paymentsList.map { payment ->
                        PaymentForFinanceTable(
                            payment.id,
                            payment.date.toString(),
                            payment.amount.toDouble(),
                        )
                    }
                )
            }
        }.awaitAll()
    }

}