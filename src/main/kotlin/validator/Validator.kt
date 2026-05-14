package com.kontenery.validator

import com.kontenery.data.ClientBankAccount
import com.kontenery.data.Contract
import com.kontenery.data.ContractDto
import com.kontenery.data.Address
import com.kontenery.service.BankAccountService
import com.kontenery.service.ContractService
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.validator(contractService: ContractService, bankAccountService: BankAccountService) {
    install(RequestValidation) {
        validate<Address> { address ->
            if (address.id != null && address.id!! <= 1)
                ValidationResult.Invalid("A customer ID should be greater than 0")
            else ValidationResult.Valid
        }

        validate<ContractDto> { contract: ContractDto ->
            println("Contract Validator")
            val productId: Long = contract.product
                ?: return@validate ValidationResult.Invalid("The contract should have product id: $contract")
            val contractWithProduct: Contract? = contractService.getCurrentByProductId(productId)
            if(contractWithProduct != null) ValidationResult.Invalid("There is another contract for this product $contractWithProduct")
            else ValidationResult.Valid
        }

        validate<ClientBankAccount> { bankAccount ->
            val accountNumber = bankAccount.bankAccount
                ?: return@validate ValidationResult.Invalid("Bank account number is required")

            if (accountNumber.isBlank())
                return@validate ValidationResult.Invalid("Bank account number cannot be blank")

            val existing = bankAccountService.findBankAccountByAccountNumber(accountNumber)
            if (existing != null)
                ValidationResult.Invalid("Bank account $accountNumber already exists (id: ${existing.id})")
            else
                ValidationResult.Valid
        }
    }
}

