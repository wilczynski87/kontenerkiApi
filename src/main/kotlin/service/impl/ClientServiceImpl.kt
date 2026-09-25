package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.Payment
import com.kontenery.data.finance.ClientFinanceDto
import com.kontenery.data.invoice.Invoice
import com.kontenery.repository.BillRepo
import com.kontenery.repository.ClientRepo
import com.kontenery.repository.InvoiceRepo
import com.kontenery.repository.PaymentRepo
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
        ensureUniqueClientIdentity(client)
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
        ensureUniqueClientIdentity(client)
        return clientRepo.updateClient(client)
    }

    private suspend fun ensureUniqueClientIdentity(client: Client) {
        client.identityEmails().forEach { email ->
            if (clientRepo.existsByEmail(email, client.id)) {
                throw IllegalArgumentException("Client with this email already exists")
            }
        }
        client.identityPesel()?.let { pesel ->
            if (clientRepo.existsByPesel(pesel, client.id)) {
                throw IllegalArgumentException("Client with this PESEL already exists")
            }
        }
    }

    override suspend fun paysVat(clientId: Long): Boolean {
        return clientRepo.paysVat(clientId)
    }

    override suspend fun finanseForClient(clientId: Long, from: LocalDate, to: LocalDate): ClientFinanceDto {
        // New / unknown client → zero balance (caller may open the gate)
        if (clientRepo.findClientById(clientId) == null) {
            return zeroFinance(clientId, from, to)
        }

        return try {
            coroutineScope {
                val payments = async { loadAllPayments(clientId, from, to) }
                val invoices = async { loadAllInvoices(clientId, from, to) }
                val bills = async { loadAllBills(clientId, from, to) }

                val paymentSum = payments.await().sumOf { it.amount }
                val billsSum = bills.await().sumOf { it.priceSum?.toBigDecimal() ?: BigDecimal.ZERO }
                val invoiceSum = invoices.await().sumOf { it.priceWithVatSum?.toBigDecimal() ?: BigDecimal.ZERO }

                val totalBalance = paymentSum - invoiceSum.setScale(2, RoundingMode.UP) - billsSum.setScale(2, RoundingMode.UP)

                ClientFinanceDto(
                    clientId,
                    from,
                    to,
                    paymentSum.toDouble(),
                    (invoiceSum + billsSum).toDouble(),
                    totalBalance.toDouble(),
                )
            }
        } catch (e: Exception) {
            println("ClientFinanceDto: $e")
            zeroFinance(clientId, from, to)
        }
    }

    private suspend fun loadAllPayments(clientId: Long, from: LocalDate, to: LocalDate): List<Payment> {
        val all = mutableListOf<Payment>()
        var page = 0
        while (true) {
            val chunk = paymentRepo.getPaymentsByClient(page, PAGE_SIZE, clientId, from, to)
            all += chunk
            if (chunk.size < PAGE_SIZE) break
            page++
        }
        return all
    }

    private suspend fun loadAllInvoices(clientId: Long, from: LocalDate, to: LocalDate): List<Invoice> {
        val all = mutableListOf<Invoice>()
        var page = 0
        while (true) {
            val chunk = invoiceRepo.getInvoicesForClient(page, PAGE_SIZE, clientId, from, to)
            all += chunk
            if (chunk.size < PAGE_SIZE) break
            page++
        }
        return all
    }

    private suspend fun loadAllBills(clientId: Long, from: LocalDate, to: LocalDate): List<Invoice> {
        val all = mutableListOf<Invoice>()
        var page = 0
        while (true) {
            val chunk = billRepo.getBillsForClient(page, PAGE_SIZE, clientId, from, to)
            all += chunk
            if (chunk.size < PAGE_SIZE) break
            page++
        }
        return all
    }

    private fun zeroFinance(clientId: Long, from: LocalDate, to: LocalDate) = ClientFinanceDto(
        clientId = clientId,
        from = from,
        to = to,
        income = 0.0,
        documentBalance = 0.0,
        totalBalance = 0.0,
    )

    companion object {
        private const val PAGE_SIZE = 500
    }
}

private fun Client.identityEmails(): List<String> = listOfNotNull(
    clientPrivate?.email.normalizeEmail(),
    clientCompany?.email.normalizeEmail(),
).distinct()

private fun Client.identityPesel(): String? =
    clientPrivate?.pesel?.trim()?.takeUnless { it.isBlank() }

private fun String?.normalizeEmail(): String? =
    this?.trim()?.takeUnless { it.isBlank() }?.lowercase()
