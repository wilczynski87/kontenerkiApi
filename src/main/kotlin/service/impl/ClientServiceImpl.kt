package com.kontenery.service.impl

import com.kontenery.library.model.Client
import com.kontenery.library.model.ClientOnList
import com.kontenery.repository.ClientRepo
import com.kontenery.service.ClientService
import java.math.BigDecimal

class ClientServiceImpl(
    private val clientRepo: ClientRepo,

) :ClientService {

    override suspend fun getClientList(page: Int, size: Int): List<ClientOnList> {
        val clients: List<Client> = getAllClients(page, size)
//        println("clients, to clientOnList: $clients")
//        clients.forEach{x -> println(x.needInvoice())}
        return clients.map { clientToClientOnList(it) }
    }

    private fun clientToClientOnList(client:Client):ClientOnList {
        return ClientOnList(
            id = client.id ?: 0,
            name = client.getName(),
            paymentsOverdue = BigDecimal.ZERO,
            contracts = null,
            active = client.isActive ?: false,
            invoice = client.needInvoice(),
            lastBill = null
        )
    }

    override suspend fun save(client: Client): Client? {
        val newClient: Client = if(client.clientCompany != null) {
                client.copy(clientCompany = client.clientCompany!!.copy(needInvoice = true))
            } else client
        return clientRepo.save(newClient)
    }

    override suspend fun getAllClients(page: Int, size: Int): List<Client> {
        return clientRepo.getAllClients(page, size)
    }

    override suspend fun findClientById(id: Long): Client? {
        return clientRepo.findClientById(id)
    }

    override suspend fun updateClient(client: Client): Client? {
        return clientRepo.updateClient(client)
    }
}