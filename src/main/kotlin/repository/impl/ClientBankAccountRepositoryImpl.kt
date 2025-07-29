package com.kontenery.repository.impl

import com.kontenery.library.model.Client
import com.kontenery.library.model.ClientBankAccount
import com.kontenery.repository.ClientBankAccountRepository
import com.kontenery.repository.entity.ClientBankAccountEntity
import com.kontenery.repository.entity.ClientBankAccountTable
import com.kontenery.repository.entity.ClientEntity
import com.kontenery.repository.entity.suspendTransaction

class ClientBankAccountRepositoryImpl: ClientBankAccountRepository {

    override suspend fun save(dto: ClientBankAccount): ClientBankAccount = suspendTransaction {
        val clientEntity = dto.client?.id?.let { ClientEntity.findById(it) }

        val entity = ClientBankAccountEntity.new {
            client = clientEntity
            bankAccount = dto.bankAccount
            createdAt = dto.createdAt
        }
        entity.toDomain()
    }

    override suspend fun get(id: Long): ClientBankAccount? = suspendTransaction {
        ClientBankAccountEntity.findById(id)?.toDomain()
    }

    override suspend fun getAllForClient(clientId: Long): List<ClientBankAccount> = suspendTransaction {
        ClientBankAccountEntity
            .find { ClientBankAccountTable.client eq clientId }
            .map { it.toDomain() }
    }

    override suspend fun findClientByAccountNumber(accountNumber: String): Client? = suspendTransaction {
        ClientBankAccountEntity
            .find { ClientBankAccountTable.bankAccount eq accountNumber }
            .firstOrNull()
            ?.client
            ?.toClient()
    }

    override suspend fun findBankAccountByAccountNumber(accountNumber: String): ClientBankAccount?  = suspendTransaction {
        ClientBankAccountEntity
            .find { ClientBankAccountTable.bankAccount eq accountNumber }
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun update(id: Long, updated: ClientBankAccount): ClientBankAccount? = suspendTransaction {
        val entity = ClientBankAccountEntity.findById(id) ?: return@suspendTransaction null
        entity.client = updated.client?.id?.let { ClientEntity.findById(it) }
        entity.bankAccount = updated.bankAccount
        entity.createdAt = updated.createdAt
        entity.toDomain()
    }

    override suspend fun delete(id: Long): Boolean = suspendTransaction {
        ClientBankAccountEntity.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }

}