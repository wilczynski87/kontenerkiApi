package com.kontenery.repository

import com.kontenery.data.Client

interface ClientRepo {

    suspend fun save(client: Client): Client?

    suspend fun findClientByEmail(email: String): Client?

    suspend fun findClientByGoogleSub(googleSub: String): Client?

    /** Links Google subject to client when unused or already linked to the same client. */
    suspend fun linkGoogleSub(clientId: Long, googleSub: String): Boolean

    suspend fun existsByEmail(email: String, excludeClientId: Long? = null): Boolean

    suspend fun existsByPesel(pesel: String, excludeClientId: Long? = null): Boolean

    suspend fun getAllClients(page:Int, size:Int): List<Client>

    suspend fun clientsListSize(): Long

    suspend fun getFilteredClients(active: Boolean, paysVat: Boolean?): List<Client>

    suspend fun findClientById(id:Long): Client?

    suspend fun updateClient(client:Client): Client?

    suspend fun paysVat(clientId: Long): Boolean
}