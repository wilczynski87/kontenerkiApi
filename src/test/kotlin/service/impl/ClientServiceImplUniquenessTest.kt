package com.kontenery.service.impl

import com.kontenery.data.Client
import com.kontenery.data.ClientCompanyData
import com.kontenery.data.ClientPersonalData
import com.kontenery.repository.BillRepo
import com.kontenery.repository.ClientRepo
import com.kontenery.repository.InvoiceRepo
import com.kontenery.repository.PaymentRepo
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ClientServiceImplUniquenessTest {

    private lateinit var clientRepo: ClientRepo
    private lateinit var paymentRepo: PaymentRepo
    private lateinit var invoiceRepo: InvoiceRepo
    private lateinit var billRepo: BillRepo
    private lateinit var service: ClientServiceImpl

    @BeforeEach
    fun setUp() {
        clientRepo = mockk()
        paymentRepo = mockk()
        invoiceRepo = mockk()
        billRepo = mockk()
        service = ClientServiceImpl(clientRepo, paymentRepo, invoiceRepo, billRepo)
    }

    @Test
    fun `save rejects duplicate personal email`() = runTest {
        coEvery { clientRepo.existsByEmail("jan@example.com", null) } returns true

        val thrown = assertThrows<IllegalArgumentException> {
            service.save(Client(clientPrivate = ClientPersonalData(email = "Jan@Example.com")))
        }

        assertEquals("Client with this email already exists", thrown.message)
        coVerify(exactly = 0) { clientRepo.save(any()) }
    }

    @Test
    fun `save rejects duplicate company email`() = runTest {
        coEvery { clientRepo.existsByEmail("biuro@firma.pl", null) } returns true

        val thrown = assertThrows<IllegalArgumentException> {
            service.save(Client(clientCompany = ClientCompanyData(email = "biuro@firma.pl")))
        }

        assertEquals("Client with this email already exists", thrown.message)
        coVerify(exactly = 0) { clientRepo.save(any()) }
    }

    @Test
    fun `save rejects duplicate pesel`() = runTest {
        coEvery { clientRepo.existsByEmail(any(), null) } returns false
        coEvery { clientRepo.existsByPesel("90010112345", null) } returns true

        val thrown = assertThrows<IllegalArgumentException> {
            service.save(
                Client(
                    clientPrivate = ClientPersonalData(
                        email = "jan@example.com",
                        pesel = "90010112345",
                    ),
                ),
            )
        }

        assertEquals("Client with this PESEL already exists", thrown.message)
        coVerify(exactly = 0) { clientRepo.save(any()) }
    }

    @Test
    fun `save persists client when email and pesel are unique`() = runTest {
        val toSave = Client(clientPrivate = ClientPersonalData(email = "jan@example.com", pesel = "90010112345"))
        val saved = toSave.copy(id = 1L)
        coEvery { clientRepo.existsByEmail("jan@example.com", null) } returns false
        coEvery { clientRepo.existsByPesel("90010112345", null) } returns false
        coEvery { clientRepo.save(any()) } returns saved

        val result = service.save(toSave)

        assertEquals(saved, result)
        coVerify { clientRepo.save(any()) }
    }

    @Test
    fun `update rejects email belonging to another client`() = runTest {
        coEvery { clientRepo.existsByEmail("zajety@example.com", 5L) } returns true

        val thrown = assertThrows<IllegalArgumentException> {
            service.updateClient(
                Client(
                    id = 5L,
                    clientPrivate = ClientPersonalData(email = "zajety@example.com"),
                ),
            )
        }

        assertEquals("Client with this email already exists", thrown.message)
        coVerify(exactly = 0) { clientRepo.updateClient(any()) }
    }

    @Test
    fun `update allows client to keep own email and pesel`() = runTest {
        val client = Client(
            id = 5L,
            clientPrivate = ClientPersonalData(email = "jan@example.com", pesel = "90010112345"),
        )
        coEvery { clientRepo.existsByEmail("jan@example.com", 5L) } returns false
        coEvery { clientRepo.existsByPesel("90010112345", 5L) } returns false
        coEvery { clientRepo.updateClient(client) } returns client

        val result = service.updateClient(client)

        assertEquals(client, result)
        coVerify { clientRepo.updateClient(client) }
    }

    @Test
    fun `save skips uniqueness check for blank email and pesel`() = runTest {
        val toSave = Client(clientPrivate = ClientPersonalData(email = "  ", pesel = ""))
        val saved = toSave.copy(id = 2L)
        coEvery { clientRepo.save(any()) } returns saved

        val result = service.save(toSave)

        assertEquals(saved, result)
        coVerify(exactly = 0) { clientRepo.existsByEmail(any(), any()) }
        coVerify(exactly = 0) { clientRepo.existsByPesel(any(), any()) }
    }
}
