package com.kontenery.repository.impl

import com.kontenery.data.Client
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
import com.kontenery.repository.entity.WorkerEntity
import com.kontenery.repository.entity.WorkerTable
import com.kontenery.repository.entity.suspendTransaction
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WorkerRepoImplTest {

    private lateinit var clientRepo: ClientRepoImpl
    private lateinit var workerRepo: WorkerRepoImpl
    private val addressRepo: AddressRepo = mockk(relaxed = true)

    @BeforeAll
    fun setupDb() {
        Database.connect("jdbc:h2:mem:worker_repo;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(
                AddressTable,
                ClientPersonalDataTable,
                ClientCompanyDataTable,
                ClientTable,
                ClientBankAccountTable,
                WorkerTable,
            )
        }
        clientRepo = ClientRepoImpl(addressRepo)
        workerRepo = WorkerRepoImpl()
    }

    @AfterAll
    fun tearDownDb() {
        transaction {
            SchemaUtils.drop(
                WorkerTable,
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
            WorkerEntity.all().forEach { it.delete() }
            ClientBankAccountEntity.all().forEach { it.delete() }
            ClientEntity.all().forEach { it.delete() }
            ClientPersonalDataEntity.all().forEach { it.delete() }
            ClientCompanyDataEntity.all().forEach { it.delete() }
        }
    }

    @Test
    fun `createWorker persists name email and password`() = runBlocking {
        val client = clientRepo.save(
            Client(
                clientPrivate = ClientPersonalData(email = "klient@example.com"),
                isActive = true,
            ),
        )

        val worker = workerRepo.createWorker(
            clientId = client.id!!,
            name = "Jan Pracownik",
            email = "worker@example.com",
            password = "haslo123",
        )

        assertNotNull(worker.id)
        assertEquals("Jan Pracownik", worker.name)
        assertEquals("worker@example.com", worker.email)
        assertEquals("haslo123", worker.password)
        assertEquals(client.id, worker.clientId)
    }

    @Test
    fun `findWorkersByEmail returns worker for login`() = runBlocking {
        val client = clientRepo.save(
            Client(clientPrivate = ClientPersonalData(email = "klient@example.com")),
        )
        workerRepo.createWorker(client.id!!, "Anna", "anna@example.com", "sekret")

        val found = workerRepo.findWorkersByEmail("anna@example.com")

        assertEquals(1, found.size)
        assertEquals("Anna", found.first().name)
    }

    @Test
    fun `deleteWorker removes worker`() = runBlocking {
        val client = clientRepo.save(
            Client(clientPrivate = ClientPersonalData(email = "klient@example.com")),
        )
        val worker = workerRepo.createWorker(client.id!!, "Usuwany", "del@example.com", "pass")

        val deleted = workerRepo.deleteWorker(worker.id!!)

        assertTrue(deleted)
        assertNull(workerRepo.findWorkerById(worker.id!!))
    }

    @Test
    fun `emailExistsForClient detects duplicate email for same client`() = runBlocking {
        val client = clientRepo.save(
            Client(clientPrivate = ClientPersonalData(email = "klient@example.com")),
        )
        workerRepo.createWorker(client.id!!, "Pierwszy", "dup@example.com", "pass")

        assertTrue(workerRepo.emailExistsForClient(client.id!!, "dup@example.com"))
        assertFalse(workerRepo.emailExistsForClient(client.id!!, "inny@example.com"))
    }
}
