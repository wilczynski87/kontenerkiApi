package com.kontenery.validator

import com.kontenery.model.Address
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.validator() {
    install(RequestValidation) {
        validate<Address> { address ->
            if (address.id != null && address.id!! <= 1)
                ValidationResult.Invalid("A customer ID should be greater than 0")
            else ValidationResult.Valid
        }
    }
}