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
class ClientRepoImplUpdateTest {

    private lateinit var repository: ClientRepoImpl
    private val addressRepo: AddressRepo = mockk(relaxed = true)

    @BeforeAll
    fun setupDb() {
        Database.connect("jdbc:h2:mem:client_repo_update;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
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
    fun `updateClient merges existing personal data fields`() = runBlocking {
        val saved = repository.save(
            Client(
                clientPrivate = ClientPersonalData(
                    firstName = "Jan",
                    lastName = "Kowalski",
                    email = "jan@example.com",
                    pesel = "90010112345",
                ),
                isActive = true,
            )
        )

        val updated = repository.updateClient(
            saved.copy(
                clientPrivate = ClientPersonalData(
                    firstName = "Janusz",
                    email = "janusz@example.com",
                ),
            )
        )

        assertNotNull(updated)
        assertEquals("Janusz", updated?.clientPrivate?.firstName)
        assertEquals("Kowalski", updated?.clientPrivate?.lastName)
        assertEquals("janusz@example.com", updated?.clientPrivate?.email)
        assertEquals("90010112345", updated?.clientPrivate?.pesel)
    }

    @Test
    fun `updateClient creates personal data for company-only client`() = runBlocking {
        val saved = repository.save(
            Client(
                clientCompany = ClientCompanyData(
                    name = "Firma Sp. z o.o.",
                    email = "biuro@firma.pl",
                    needInvoice = true,
                ),
                isActive = true,
            )
        )

        val updated = repository.updateClient(
            saved.copy(
                clientPrivate = ClientPersonalData(
                    firstName = "Anna",
                    lastName = "Nowak",
                    email = "anna@example.com",
                ),
            )
        )

        assertNotNull(updated)
        assertEquals("Anna", updated?.clientPrivate?.firstName)
        assertEquals("Nowak", updated?.clientPrivate?.lastName)
        assertEquals("anna@example.com", updated?.clientPrivate?.email)
        assertEquals("Firma Sp. z o.o.", updated?.clientCompany?.name)
    }

    @Test
    fun `updateClient creates company data when missing`() = runBlocking {
        val saved = repository.save(
            Client(
                clientPrivate = ClientPersonalData(
                    firstName = "Jan",
                    lastName = "Kowalski",
                    email = "jan@example.com",
                ),
                isActive = true,
            )
        )

        val updated = repository.updateClient(
            saved.copy(
                clientCompany = ClientCompanyData(
                    name = "Nowa Firma",
                    nip = "8943278612",
                    needInvoice = true,
                ),
            )
        )

        assertNotNull(updated)
        assertEquals("Nowa Firma", updated?.clientCompany?.name)
        assertEquals("8943278612", updated?.clientCompany?.nip)
        assertEquals(true, updated?.clientCompany?.needInvoice)
    }

    @Test
    fun `updateClient sets updatedAt and invoiceTitle`() = runBlocking {
        val saved = repository.save(
            Client(
                clientPrivate = ClientPersonalData(email = "jan@example.com"),
                invoiceTitle = "Stary tytuł",
                isActive = true,
            )
        )

        assertNull(saved.updatedAt)

        val updated = repository.updateClient(
            saved.copy(invoiceTitle = "Nowy tytuł faktury")
        )

        assertNotNull(updated?.updatedAt)
        assertEquals("Nowy tytuł faktury", updated?.invoiceTitle)
    }

    @Test
    fun `updateClient updates password only`() = runBlocking {
        val saved = repository.save(
            Client(
                password = "stare",
                clientPrivate = ClientPersonalData(
                    firstName = "Jan",
                    email = "jan@example.com",
                    pesel = "90010112345",
                ),
                isActive = true,
            )
        )

        val updated = repository.updateClient(
            saved.copy(password = "noweHaslo")
        )

        assertEquals("noweHaslo", updated?.password)
        assertEquals("Jan", updated?.clientPrivate?.firstName)
        assertEquals("jan@example.com", updated?.clientPrivate?.email)
        assertEquals(true, updated?.isActive)
        assertNotNull(updated?.updatedAt)
    }

    @Test
    fun `updateClient preserves custom salutation on partial personal data update`() = runBlocking {
        val saved = repository.save(
            Client(
                clientPrivate = ClientPersonalData(
                    firstName = "Jan",
                    email = "jan@example.com",
                    salutation = "Szanowny Panie",
                ),
                isActive = true,
            )
        )

        val updated = repository.updateClient(
            saved.copy(
                clientPrivate = ClientPersonalData(email = "nowy@example.com"),
            )
        )

        assertEquals("nowy@example.com", updated?.clientPrivate?.email)
        assertEquals("Szanowny Panie", updated?.clientPrivate?.salutation)
    }

    @Test
    fun `updateClient returns null when client does not exist`() = runBlocking {
        val result = repository.updateClient(
            Client(id = 999L, clientPrivate = ClientPersonalData(email = "x@y.z"))
        )

        assertNull(result)
    }
}
