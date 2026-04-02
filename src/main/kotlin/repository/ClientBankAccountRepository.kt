package com.kontenery.repository

import com.kontenery.data.Client
import com.kontenery.data.ClientBankAccount

interface ClientBankAccountRepository {

    suspend fun save(dto: ClientBankAccount): ClientBankAccount?

    suspend fun get(id: Long): ClientBankAccount?

    suspend fun getAllForClient(clientId: Long): List<ClientBankAccount>

    suspend fun findClientByAccountNumber(accountNumber: String): Client?

    suspend fun findBankAccountByAccountNumber(accountNumber: String): ClientBankAccount?

    suspend fun update(id: Long, updated: ClientBankAccount): ClientBankAccount?

    suspend fun delete(id: Long): Boolean
}