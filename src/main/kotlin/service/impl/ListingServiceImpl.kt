package com.kontenery.service.impl

import com.kontenery.library.model.Client
import com.kontenery.library.model.ClientOnList
import com.kontenery.library.model.Payment
import com.kontenery.library.model.Product
import com.kontenery.library.model.invoice.Invoice
import com.kontenery.library.utils.endOfCurrentMonth
import com.kontenery.library.utils.endOfCurrentYear
import com.kontenery.library.utils.startOfCurrentYear
import com.kontenery.repository.*
import com.kontenery.service.ListingService
import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class ListingServiceImpl(
    private val clientsRepo: ClientRepo,
    private val productsRepo: ProductRepo,
    private val contractsRepo: ContractRepo,
    private val paymentsRepo: PaymentRepo,
    private val invoicesRepo: InvoiceRepo
): ListingService {
    override suspend fun clientsList(page: Int, size: Int): List<ClientOnList> {
        val clients: List<Client> = clientsRepo.getAllClients(page, size)
        return clients.mapNotNull { clientToClientOnList(it) }
    }

    private suspend fun clientToClientOnList(client: Client): ClientOnList? {
        if(client.id == null) throw NullPointerException("Client dose not have ID: $client")
        val from: LocalDate = LocalDate.startOfCurrentYear()
        val to: LocalDate = LocalDate.endOfCurrentYear()
        return try {
            ClientOnList(
                id = client.id!!,
                name = client.getName(),
                paymentsOverdue = clientOverdue(client.id!!, from, to),
                contracts = contractsRepo.findByClientId(client.id!!, true).toString(),
                active = client.isActive ?: false,
                invoice = client.needInvoice(),
                lastBill = clientLastInvoiceSend(client.id!!)
            )
        } catch (e: Exception) {
            println("clientToClientOnList: $e")
            null
        }
    }

    private suspend fun clientOverdue(clientId: Long, from: LocalDate, to: LocalDate): BigDecimal? {
        return try {
            val payments: List<Payment> = paymentsRepo.getPaymentsByClient(0, 1000, clientId, from, to)
            val invoices: List<Invoice> = invoicesRepo.getInvoicesForClient(0, 1000, clientId, from, to)

            val paymentSum = payments.sumOf { it.amount }
            val invoiceSum = invoices.sumOf { it.priceWithVatSum?.toBigDecimal() ?: it.priceSum?.toBigDecimal() ?: BigDecimal.ZERO }

            paymentSum - invoiceSum.setScale(2, RoundingMode.UP)
        } catch (e: Exception) {
            println("clientOverdue: $e")
            null
        }
    }
    private suspend fun clientLastInvoiceSend(clientId: Long): LocalDate? {
        val lastInvoice: Invoice? = invoicesRepo.getLastInvoiceForClient(clientId)

        return lastInvoice?.invoiceSendToClient

    }

    override suspend fun productList(page: Int, size: Int): List<Product> {
        return productsRepo.getAllProduct(page, size)
    }

}