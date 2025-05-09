package com.kontenery.validator

import com.kontenery.model.Address
import com.kontenery.model.Contract
import com.kontenery.model.ContractDto
import com.kontenery.service.ContractService
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.validator(contractService: ContractService) {
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
    }
}

