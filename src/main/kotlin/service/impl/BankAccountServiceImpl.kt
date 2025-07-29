package com.kontenery.service.impl

import com.kontenery.library.model.Client
import com.kontenery.library.model.ClientBankAccount
import com.kontenery.repository.ClientBankAccountRepository
import com.kontenery.service.BankAccountService

class BankAccountServiceImpl(private val repo: ClientBankAccountRepository): BankAccountService {

    override suspend fun save(dto: ClientBankAccount): ClientBankAccount? {
        val updatedBankAccount = dto.copy(bankAccount = dto.bankAccount?.filterNot { it.isWhitespace() })
        return repo.save(updatedBankAccount)
    }

    override suspend fun get(id: Long): ClientBankAccount? {
        return repo.get(id)
    }

    override suspend fun getAllForClient(clientId: Long): List<ClientBankAccount> {
        return repo.getAllForClient(clientId)
    }

    override suspend fun findClientByAccountNumber(accountNumber: String): Client? {
        val updatedBankAccount: String = accountNumber.filterNot { it.isWhitespace() }
        return repo.findClientByAccountNumber(updatedBankAccount)
    }

    override suspend fun findBankAccountByAccountNumber(accountNumber: String): ClientBankAccount? {
        val updatedBankAccount: String = accountNumber.filterNot { it.isWhitespace() }
        return repo.findBankAccountByAccountNumber(updatedBankAccount)
    }

    override suspend fun update(id: Long, updated: ClientBankAccount): ClientBankAccount? {
        return repo.update(id, updated)
    }

    override suspend fun delete(id: Long): Boolean {
        return repo.delete(id)
    }
}