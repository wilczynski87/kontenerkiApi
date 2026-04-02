package com.kontenery.repository

import com.kontenery.data.Client

interface ClientRepo {

    suspend fun save(client: Client): Client?

    suspend fun getAllClients(page:Int, size:Int): List<Client>

    suspend fun clientsListSize(): Long

    suspend fun getFilteredClients(active: Boolean, paysVat: Boolean?): List<Client>

    suspend fun findClientById(id:Long): Client?

    suspend fun updateClient(client:Client): Client?

    suspend fun paysVat(clientId: Long): Boolean
}