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
import com.kontenery.repository.entity.GateEventEntity
import com.kontenery.repository.entity.GateEventTable
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GateEventRepoImplTest {

    private lateinit var clientRepo: ClientRepoImpl
    private lateinit var workerRepo: WorkerRepoImpl
    private lateinit var gateEventRepo: GateEventRepoImpl
    private val addressRepo: AddressRepo = mockk(relaxed = true)

    @BeforeAll
    fun setupDb() {
        Database.connect("jdbc:h2:mem:gate_event_repo;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(
                AddressTable,
                ClientPersonalDataTable,
                ClientCompanyDataTable,
                ClientTable,
                ClientBankAccountTable,
                WorkerTable,
                GateEventTable,
            )
        }
        clientRepo = ClientRepoImpl(addressRepo)
        workerRepo = WorkerRepoImpl()
        gateEventRepo = GateEventRepoImpl()
    }

    @AfterAll
    fun tearDownDb() {
        transaction {
            SchemaUtils.drop(
                GateEventTable,
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
            GateEventEntity.all().forEach { it.delete() }
            WorkerEntity.all().forEach { it.delete() }
            ClientBankAccountEntity.all().forEach { it.delete() }
            ClientEntity.all().forEach { it.delete() }
            ClientPersonalDataEntity.all().forEach { it.delete() }
            ClientCompanyDataEntity.all().forEach { it.delete() }
        }
    }

    @Test
    fun `logOpenEvent persists worker id when provided`() = runBlocking {
        val client = clientRepo.save(Client(clientPrivate = ClientPersonalData(email = "klient@example.com")))
        val worker = workerRepo.createWorker(client.id!!, "Jan", "jan@example.com", "pass")

        gateEventRepo.logOpenEvent(client.id!!, worker.id!!, note = "yard")

        val events = gateEventRepo.listOpenEventsByWorkerId(worker.id!!)
        assertEquals(1, events.size)
        assertTrue(events.single().openedAtEpochMs > 0)
    }

    @Test
    fun `logOpenEvent without worker id is excluded from worker history`() = runBlocking {
        val client = clientRepo.save(Client(clientPrivate = ClientPersonalData(email = "klient@example.com")))
        val worker = workerRepo.createWorker(client.id!!, "Jan", "jan@example.com", "pass")

        gateEventRepo.logOpenEvent(client.id!!, workerId = null, note = "yard")
        gateEventRepo.logOpenEvent(client.id!!, worker.id!!, note = "yard")

        val events = gateEventRepo.listOpenEventsByWorkerId(worker.id!!)
        assertEquals(1, events.size)
    }

    @Test
    fun `listOpenEventsByWorkerId returns newest first and respects limit`() = runBlocking {
        val client = clientRepo.save(Client(clientPrivate = ClientPersonalData(email = "klient@example.com")))
        val worker = workerRepo.createWorker(client.id!!, "Jan", "jan@example.com", "pass")

        repeat(3) {
            gateEventRepo.logOpenEvent(client.id!!, worker.id!!, note = "yard")
        }

        val limited = gateEventRepo.listOpenEventsByWorkerId(worker.id!!, limit = 2)
        assertEquals(2, limited.size)
        assertTrue(limited[0].openedAtEpochMs >= limited[1].openedAtEpochMs)
    }

    @Test
    fun `getLastOpenEventEpochMs returns latest client event regardless of worker`() = runBlocking {
        val client = clientRepo.save(Client(clientPrivate = ClientPersonalData(email = "klient@example.com")))
        val worker = workerRepo.createWorker(client.id!!, "Jan", "jan@example.com", "pass")

        gateEventRepo.logOpenEvent(client.id!!, workerId = null, note = "yard")
        gateEventRepo.logOpenEvent(client.id!!, worker.id!!, note = "yard")

        val last = gateEventRepo.getLastOpenEventEpochMs(client.id!!)
        assertTrue(last != null)
        assertEquals(
            gateEventRepo.listOpenEventsByWorkerId(worker.id!!).maxOf { it.openedAtEpochMs },
            last,
        )
    }
}
