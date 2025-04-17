package com.kontenery.service

import com.kontenery.model.Client
import com.kontenery.model.ClientOnList

interface ClientService {

    suspend fun getClientList(page: Int, size: Int): List<ClientOnList>

    suspend fun save(client: Client): Client?

    suspend fun getAllClients(page:Int, size:Int): List<Client>

    suspend fun findClientById(id:Long): Client?

    suspend fun updateClient(client: Client): Client?
}