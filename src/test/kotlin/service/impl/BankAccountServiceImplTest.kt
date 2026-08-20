package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.ClientBankAccount
import com.kontenery.repository.ClientBankAccountRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BankAccountServiceImplTest {

    private lateinit var repo: ClientBankAccountRepository
    private lateinit var service: BankAccountServiceImpl

    @BeforeEach
    fun setUp() {
        repo = mockk()
        service = BankAccountServiceImpl(repo)
    }

    @Test
    fun `save normalizes account number with PL prefix`() = runTest {
        val slot = slot<ClientBankAccount>()
        coEvery { repo.save(capture(slot)) } answers { slot.captured.copy(id = 1L) }

        service.save(
            ClientBankAccount(
                bankAccount = "25114010100000536605001001",
                client = Client(id = 48L),
                createdAt = LocalDate(2026, 8, 20),
            )
        )

        assertEquals("PL25114010100000536605001001", slot.captured.bankAccount)
        coVerify(exactly = 1) { repo.save(any()) }
    }
}
