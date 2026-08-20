package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.ClientBankAccount
import com.kontenery.data.utils.BankAccount
import com.kontenery.repository.ClientBankAccountRepository
import com.kontenery.service.BankAccountService

class BankAccountServiceImpl(private val repo: ClientBankAccountRepository): BankAccountService {

    override suspend fun save(dto: ClientBankAccount): ClientBankAccount? {
        val updatedBankAccount = dto.copy(
            bankAccount = dto.bankAccount?.let { BankAccount.normalize(it) }
        )
        return repo.save(updatedBankAccount)
    }

    override suspend fun get(id: Long): ClientBankAccount? {
        return repo.get(id)
    }

    override suspend fun getAllForClient(clientId: Long): List<ClientBankAccount> {
        return repo.getAllForClient(clientId)
    }

    override suspend fun findClientByAccountNumber(accountNumber: String): Client? {
        return repo.findClientByAccountNumber(accountNumber)
    }

    override suspend fun findBankAccountByAccountNumber(accountNumber: String): ClientBankAccount? {
        return repo.findBankAccountByAccountNumber(accountNumber)
    }

    override suspend fun update(id: Long, updated: ClientBankAccount): ClientBankAccount? {
        val normalized = updated.copy(
            bankAccount = updated.bankAccount?.let { BankAccount.normalize(it) }
        )
        return repo.update(id, normalized)
    }

    override suspend fun delete(id: Long): Boolean {
        return repo.delete(id)
    }
}
