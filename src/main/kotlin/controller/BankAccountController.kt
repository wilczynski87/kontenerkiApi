package com.kontenery.controller

import com.kontenery.data.ClientBankAccount
import com.kontenery.service.BankAccountService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.bankAccountController(bankAccountService: BankAccountService) {
    route("/bankAccount") {

        post("add") {
            try {
                val bankAccount: ClientBankAccount = call.receive<ClientBankAccount>()
                val savedBankAccount: ClientBankAccount? = bankAccountService.save(bankAccount)

                call.respondNullable(HttpStatusCode.OK, savedBankAccount)
            } catch (e: Exception) {
                println("Exception in /bankAccount/add: ${e.message}")
                call.respondNullable(HttpStatusCode.ExpectationFailed, e.message)
            }
        }

        get("{id}/get") {
            println("/bankAccount/ GET ")
            try {
                val bankAccountId: Long? = call.request.pathVariables["id"]?.toLongOrNull()
                assert(bankAccountId != null)
                val savedBankAccount: ClientBankAccount? = bankAccountService.get(bankAccountId!!)

                call.respondNullable(HttpStatusCode.OK, savedBankAccount)
            } catch (e: Exception) {
                println("Exception in /bankAccount/ GET ")
                call.respondNullable(HttpStatusCode.ExpectationFailed, e.message)
            }
        }

        get("{id}/getAllForClient") {
            println("/bankAccount/ getAllForClient ")
            try {
                val clientId: Long? = call.request.pathVariables["id"]?.toLongOrNull()
                assert(clientId != null)
                val bankAccounts: List<ClientBankAccount> = bankAccountService.getAllForClient(clientId!!)

                call.respondNullable(HttpStatusCode.OK, bankAccounts)
            } catch (e: Exception) {
                println("Exception in /bankAccount/ getAllForClient ")
                call.respondNullable(HttpStatusCode.ExpectationFailed, e.message)
            }
        }

        delete("{clientId}/{accountNumber}/delete") {
            println("/bankAccount/ delete ")
            try {
                val clientId: Long? = call.request.pathVariables["clientId"]?.toLongOrNull()
                val accountNumber: String = call.request.pathVariables["accountNumber"].orEmpty()
                if(accountNumber.isBlank()) throw NullPointerException("Brak numeru konta")

                val checkedBankAccount = bankAccountService.findBankAccountByAccountNumber(accountNumber)
                println("checkedBankAccount: $checkedBankAccount")

                if (checkedBankAccount != null) {
                    bankAccountService.delete(checkedBankAccount.id!!)
                } else call.respondNullable(HttpStatusCode.FailedDependency, false)

                call.respond(HttpStatusCode.OK, true)
            } catch (e: Exception) {
                println("Exception in /bankAccount/ getAllForClient ")
                call.respondNullable(HttpStatusCode.ExpectationFailed, e.message)
            }
        }
    }
}