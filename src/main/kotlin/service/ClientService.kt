package com.kontenery.service

import com.kontenery.library.model.Client
import com.kontenery.library.model.ClientOnList


interface ClientService {

    suspend fun save(client: Client): Client?

    suspend fun getAllClients(page:Int, size:Int): List<Client>

    suspend fun getFilteredClients(active: Boolean, paysVat: Boolean? = null): List<Client>

    suspend fun findClientById(id:Long): Client?

    suspend fun updateClient(client: Client): Client?

    suspend fun paysVat(clientId: Long): Boolean
}