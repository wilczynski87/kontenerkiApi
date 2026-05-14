package com.kontenery.validator

import com.kontenery.data.ClientBankAccount
import com.kontenery.data.utils.errors.BankAccountError
import com.kontenery.data.utils.errors.ValidationErrorType
import com.kontenery.service.BankAccountService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BankAccountValidatorTest {

    private lateinit var bankAccountService: BankAccountService
    private lateinit var validator: BankAccountValidator

    @BeforeEach
    fun setUp() {
        bankAccountService = mockk()
        validator = BankAccountValidator(bankAccountService)
    }

    @Test
    fun `should return null when bank account is valid and unique`() = runTest {
        coEvery { bankAccountService.findBankAccountByAccountNumber("PL61105010701000009081619455") } returns null

        val result = validator.validate(
            ClientBankAccount(bankAccount = "PL61105010701000009081619455", createdAt = LocalDate(2026, 4, 15))
        )

        assertNull(result)
    }

    @Test
    fun `should return error when bank account number is null`() = runTest {
        val result = validator.validate(
            ClientBankAccount(bankAccount = null, createdAt = LocalDate(2026, 4, 15))
        )

        assertNotNull(result)
        assertEquals(ValidationErrorType.NOT_FOUND.name, result!!.title)
        assertEquals("Bank account number is required", result.message)
        assertNull(result.bankAccount)
        assertNull(result.existingId)
    }

    @Test
    fun `should return error when bank account number is blank`() = runTest {
        val result = validator.validate(
            ClientBankAccount(bankAccount = "   ", createdAt = LocalDate(2026, 4, 15))
        )

        assertNotNull(result)
        assertEquals(ValidationErrorType.NOT_FOUND.name, result!!.title)
        assertEquals("Bank account number cannot be blank", result.message)
        assertEquals("   ", result.bankAccount)
    }

    @Test
    fun `should return error with details when bank account already exists`() = runTest {
        val existing = ClientBankAccount(
            id = 5,
            bankAccount = "PL61105010701000009081619455",
            createdAt = LocalDate(2026, 1, 1)
        )
        coEvery { bankAccountService.findBankAccountByAccountNumber("PL61105010701000009081619455") } returns existing

        val result = validator.validate(
            ClientBankAccount(bankAccount = "PL61105010701000009081619455", createdAt = LocalDate(2026, 4, 15))
        )

        assertNotNull(result)
        assertEquals(ValidationErrorType.DUPLICATED.name, result!!.title)
        assertEquals("Bank account PL61105010701000009081619455 already exists", result.message)
        assertEquals("PL61105010701000009081619455", result.bankAccount)
        assertEquals(5L, result.existingId)
    }

    @Test
    fun `should accept different account numbers independently`() = runTest {
        coEvery { bankAccountService.findBankAccountByAccountNumber("PL11111111111111111111111111") } returns null
        coEvery { bankAccountService.findBankAccountByAccountNumber("PL22222222222222222222222222") } returns null

        val result1 = validator.validate(
            ClientBankAccount(bankAccount = "PL11111111111111111111111111", createdAt = LocalDate(2026, 4, 15))
        )
        val result2 = validator.validate(
            ClientBankAccount(bankAccount = "PL22222222222222222222222222", createdAt = LocalDate(2026, 4, 15))
        )

        assertNull(result1)
        assertNull(result2)
    }
}
