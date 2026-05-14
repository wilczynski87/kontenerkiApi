package com.kontenery.validator

import com.kontenery.data.ClientBankAccount
import com.kontenery.data.utils.errors.BankAccountError
import com.kontenery.data.utils.errors.ValidationErrorType
import com.kontenery.service.BankAccountService

class BankAccountValidator(
    private val bankAccountService: BankAccountService
) {
    suspend fun validate(bankAccount: ClientBankAccount): BankAccountError? {
        val accountNumber = bankAccount.bankAccount
            ?: return BankAccountError(
                title = ValidationErrorType.NOT_FOUND.name,
                message = "Bank account number is required"
            )

        if (accountNumber.isBlank()) {
            return BankAccountError(
                title = ValidationErrorType.NOT_FOUND.name,
                message = "Bank account number cannot be blank",
                bankAccount = accountNumber
            )
        }

        val existing = bankAccountService.findBankAccountByAccountNumber(accountNumber)
        if (existing != null) {
            return BankAccountError(
                title = ValidationErrorType.DUPLICATED.name,
                message = "Bank account $accountNumber already exists",
                bankAccount = accountNumber,
                existingId = existing.id
            )
        }

        return null
    }
}
