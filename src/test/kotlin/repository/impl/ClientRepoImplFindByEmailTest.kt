package com.kontenery.repository.impl

import com.kontenery.data.Client
import com.kontenery.data.ClientCompanyData
import com.kontenery.data.ClientPersonalData
import com.kontenery.repository.AddressRepo
import com.kontenery.repository.entity.AddressTable
import com.kontenery.repository.entity.ClientBankAccountEntity
import com.kontenery.repository.entity.ClientBankAccountTable
import com.kontenery.repository.entity.ClientCompanyDataEntity
import com.kontenery.repository.entity.ClientCompanyDataTable
import com.kontenery.repository.entity.ClientEntity
import com.kontenery.repository.entity.ClientPersonalDataEntity
import com.kontenery.repository.entity.ClientPersonalDataTable
import com.kontenery.repository.entity.ClientTable
import com.kontenery.repository.entity.suspendTransaction
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientRepoImplFindByEmailTest {

    private lateinit var repository: ClientRepoImpl
    private val addressRepo: AddressRepo = mockk(relaxed = true)

    @BeforeAll
    fun setupDb() {
        Database.connect("jdbc:h2:mem:client_repo_email;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(
                AddressTable,
                ClientPersonalDataTable,
                ClientCompanyDataTable,
                ClientTable,
                ClientBankAccountTable,
            )
        }
        repository = ClientRepoImpl(addressRepo)
    }

    @AfterAll
    fun tearDownDb() {
        transaction {
            SchemaUtils.drop(
                ClientBankAccountTable,
                ClientTable,
                ClientCompanyDataTable,
                ClientPersonalDataTable,
                AddressTable,
            )
        }
    }

    @BeforeEach
    fun clearDb() = runBlocking {
        suspendTransaction {
            ClientBankAccountEntity.all().forEach { it.delete() }
            ClientEntity.all().forEach { it.delete() }
            ClientPersonalDataEntity.all().forEach { it.delete() }
            ClientCompanyDataEntity.all().forEach { it.delete() }
        }
    }

    @Test
    fun `findClientByEmail finds client by personal email`() = runBlocking {
        val saved = repository.save(
            Client(
                password = "haslo1",
                clientPrivate = ClientPersonalData(
                    firstName = "Jan",
                    lastName = "Kowalski",
                    email = "jan@example.com",
                    pesel = "90010112345",
                ),
                isActive = true,
            )
        )

        val found = repository.findClientByEmail("jan@example.com")

        assertNotNull(found)
        assertEquals(saved.id, found?.id)
        assertEquals("haslo1", found?.password)
        assertEquals("jan@example.com", found?.clientPrivate?.email)
    }

    @Test
    fun `findClientByEmail finds client by company email`() = runBlocking {
        val saved = repository.save(
            Client(
                password = "firmowe",
                clientCompany = ClientCompanyData(
                    name = "Firma Sp. z o.o.",
                    email = "biuro@firma.pl",
                    needInvoice = true,
                ),
                isActive = true,
            )
        )

        val found = repository.findClientByEmail("biuro@firma.pl")

        assertNotNull(found)
        assertEquals(saved.id, found?.id)
        assertEquals("firmowe", found?.password)
        assertEquals("biuro@firma.pl", found?.clientCompany?.email)
    }

    @Test
    fun `findClientByEmail is case insensitive and trims input`() = runBlocking {
        repository.save(
            Client(
                clientPrivate = ClientPersonalData(email = "Jan@Example.com"),
                isActive = true,
            )
        )

        val found = repository.findClientByEmail("  jan@example.com  ")

        assertNotNull(found)
        assertEquals("Jan@Example.com", found?.clientPrivate?.email)
    }

    @Test
    fun `findClientByEmail returns null when email is unknown`() = runBlocking {
        repository.save(
            Client(
                clientPrivate = ClientPersonalData(email = "istnieje@example.com"),
                isActive = true,
            )
        )

        val found = repository.findClientByEmail("brak@example.com")

        assertNull(found)
    }

    @Test
    fun `save persists password on Client and returns it`() = runBlocking {
        val saved = repository.save(
            Client(
                password = "sekret",
                clientPrivate = ClientPersonalData(
                    email = "pass@example.com",
                    pesel = "11111111111",
                ),
            )
        )

        assertEquals("sekret", saved.password)
        assertEquals("sekret", repository.findClientById(saved.id!!)?.password)
    }
}
