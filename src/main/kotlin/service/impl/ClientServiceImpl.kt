package com.kontenery.service.impl

import com.kontenery.model.Client
import com.kontenery.repository.ClientRepo
import com.kontenery.service.ClientService

class ClientServiceImpl(private val clientRepo: ClientRepo) :ClientService {
    override suspend fun save(client: Client): Client? {
        return clientRepo.save(client)
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