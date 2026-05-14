package com.kontenery.validator

import com.kontenery.data.ClientBankAccount
import com.kontenery.service.BankAccountService
import com.kontenery.service.ContractService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BankAccountValidatorTest {

    private val bankAccountService: BankAccountService = mockk()
    private val contractService: ContractService = mockk()

    private fun ApplicationTestBuilder.configureTestApp() {
        install(ContentNegotiation) { json() }
        application {
            validator(contractService, bankAccountService)
        }
        routing {
            post("/bankAccount/add") {
                try {
                    val bankAccount = call.receive<ClientBankAccount>()
                    call.respond(HttpStatusCode.OK, "saved")
                } catch (e: RequestValidationException) {
                    call.respond(HttpStatusCode.BadRequest, e.reasons.joinToString("; "))
                }
            }
        }
    }

    @Test
    fun `should accept new bank account when number does not exist`() = testApplication {
        configureTestApp()
        coEvery { bankAccountService.findBankAccountByAccountNumber("PL61105010701000009081619455") } returns null

        val response = client.post("/bankAccount/add") {
            contentType(ContentType.Application.Json)
            setBody("""{"bankAccount":"PL61105010701000009081619455","createdAt":"2026-04-15"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `should reject bank account when number already exists`() = testApplication {
        configureTestApp()
        val existing = ClientBankAccount(
            id = 5,
            bankAccount = "PL61105010701000009081619455",
            createdAt = LocalDate(2026, 1, 1)
        )
        coEvery { bankAccountService.findBankAccountByAccountNumber("PL61105010701000009081619455") } returns existing

        val response = client.post("/bankAccount/add") {
            contentType(ContentType.Application.Json)
            setBody("""{"bankAccount":"PL61105010701000009081619455","createdAt":"2026-04-15"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("already exists"))
    }

    @Test
    fun `should reject bank account when number is null`() = testApplication {
        configureTestApp()

        val response = client.post("/bankAccount/add") {
            contentType(ContentType.Application.Json)
            setBody("""{"createdAt":"2026-04-15"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("required"))
    }

    @Test
    fun `should reject bank account when number is blank`() = testApplication {
        configureTestApp()

        val response = client.post("/bankAccount/add") {
            contentType(ContentType.Application.Json)
            setBody("""{"bankAccount":"   ","createdAt":"2026-04-15"}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("blank"))
    }

    @Test
    fun `should accept different account numbers independently`() = testApplication {
        configureTestApp()
        coEvery { bankAccountService.findBankAccountByAccountNumber("PL11111111111111111111111111") } returns null
        coEvery { bankAccountService.findBankAccountByAccountNumber("PL22222222222222222222222222") } returns null

        val response1 = client.post("/bankAccount/add") {
            contentType(ContentType.Application.Json)
            setBody("""{"bankAccount":"PL11111111111111111111111111","createdAt":"2026-04-15"}""")
        }
        val response2 = client.post("/bankAccount/add") {
            contentType(ContentType.Application.Json)
            setBody("""{"bankAccount":"PL22222222222222222222222222","createdAt":"2026-04-15"}""")
        }

        assertEquals(HttpStatusCode.OK, response1.status)
        assertEquals(HttpStatusCode.OK, response2.status)
    }
}
