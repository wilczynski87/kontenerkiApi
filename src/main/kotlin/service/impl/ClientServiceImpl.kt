package com.kontenery.service.impl

import com.kontenery.library.model.Client
import com.kontenery.library.model.ClientOnList
import com.kontenery.repository.ClientRepo
import com.kontenery.service.ClientService
import java.math.BigDecimal

class ClientServiceImpl(
    private val clientRepo: ClientRepo,

) :ClientService {

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