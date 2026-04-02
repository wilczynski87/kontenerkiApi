package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.repository.ClientRepo
import com.kontenery.service.ClientService

class ClientServiceImpl(
    private val clientRepo: ClientRepo,

) :ClientService {

    override suspend fun save(client: Client): Client? {
        val newClient: Client = if(client.clientCompany != null) {
                val needInvoice: Boolean = client.clientCompany!!.needInvoice ?: true
                client.copy(clientCompany = client.clientCompany!!.copy(needInvoice = needInvoice))
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
}