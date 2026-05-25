package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.finance.ClientFinanceDto
import com.kontenery.repository.BillRepo
import com.kontenery.repository.ClientRepo
import com.kontenery.repository.InvoiceRepo
import com.kontenery.repository.PaymentRepo
import com.kontenery.repository.impl.PaymentRepoImpl
import com.kontenery.service.ClientService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import java.math.RoundingMode

class ClientServiceImpl(
    private val clientRepo: ClientRepo,
    private val paymentRepo: PaymentRepo,
    private val invoiceRepo: InvoiceRepo,
    private val billRepo: BillRepo,
) : ClientService {

    override suspend fun save(client: Client): Client? {
        val newClient: Client = if(client.clientCompany != null) {
                val needInvoice: Boolean = client.clientCompany.needInvoice ?: true
                client.copy(clientCompany = client.clientCompany.copy(needInvoice = needInvoice))
            } else client
        return clientRepo.save(newClient)
    }

    override suspend fun getAllClients(page: Int, size: Int): List<Client> {
        return clientRepo.getAllClients(page, size)
    }

    override suspend fun getFilteredClients(active: Boolean, paysVat: Boolean?): List<Client> {
        return clientRepo.getFilteredClients(active, paysVat)
    }

    override suspend fun findClientById(id: Long): Client? {
        return clientRepo.findClientById(id)
    }

    override suspend fun updateClient(client: Client): Client? {
        return clientRepo.updateClient(client)
    }

    override suspend fun paysVat(clientId: Long): Boolean {
        return clientRepo.paysVat(clientId)
    }

    override suspend fun finanseForClient(clientId: Long, from: LocalDate, to: LocalDate): ClientFinanceDto? {
        if(clientRepo.findClientById(clientId) == null) return null

        return try {
            coroutineScope {
                val payments = async { paymentRepo.getPaymentsByClient(0, 1000, clientId, from, to) }
                val invoices = async { invoiceRepo.getInvoicesForClient(0, 1000, clientId, from, to) }
                val bills = async { billRepo.getBillsForClient(0, 1000, clientId, from, to) }

                val paymentSum = payments.await().sumOf { it.amount }
                val billsSum = bills.await().sumOf { it.priceSum?.toBigDecimal() ?: BigDecimal.ZERO }
                val invoiceSum = invoices.await().sumOf { it.priceWithVatSum?.toBigDecimal() ?: BigDecimal.ZERO }

                val totalBalance = paymentSum - invoiceSum.setScale(2, RoundingMode.UP) - billsSum.setScale(2, RoundingMode.UP)

                ClientFinanceDto(
                    clientId,
                    from,
                    to,
                    paymentSum.toDouble(),
                    invoiceSum.toDouble(),
                    totalBalance.toDouble(),
                )
            }
        } catch (e: Exception) {
            println("ClientFinanceDto: $e")
            null
        }
    }
}