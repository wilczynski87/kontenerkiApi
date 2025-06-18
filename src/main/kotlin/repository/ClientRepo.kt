package com.kontenery.repository

import com.kontenery.library.model.Client

interface ClientRepo {

    suspend fun save(client: Client): Client?

    suspend fun getAllClients(page:Int, size:Int): List<Client>

    suspend fun findClientById(id:Long): Client?

    suspend fun updateClient(client:Client): Client?
}