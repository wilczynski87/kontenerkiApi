package repository.impl

import com.kontenery.library.model.Client
import com.kontenery.library.model.ClientBankAccount
import com.kontenery.library.utils.now
import com.kontenery.repository.entity.*
import com.kontenery.repository.impl.ClientBankAccountRepositoryImpl
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*

import org.junit.jupiter.api.Assertions.*
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientBankAccountRepositoryImplTest {

    private val repository = ClientBankAccountRepositoryImpl()

    @Test
    fun sampleTest() {
        assertEquals(2, 1 + 1)
    }

    @BeforeAll
    fun setupDb() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        TransactionManager.defaultDatabase?.let {
            transaction(it) {
                SchemaUtils.create(ClientTable, ClientBankAccountTable)
            }
        }
    }

    @BeforeEach
    fun clearDb() = runBlocking {
        suspendTransaction {
            ClientBankAccountEntity.all().forEach { it.delete() }
            ClientEntity.all().forEach { it.delete() }
        }
    }

    private suspend fun createClient(name: String): ClientEntity = suspendTransaction {
        ClientEntity.new {
            this.createdAt = LocalDate.now()
        }
    }

    @Test
    fun `should save and retrieve ClientBankAccount`(): Unit = runBlocking {
        val client = createClient("Test Client")

        val saved = repository.save(
            ClientBankAccount(
                bankAccount = "PL1234567890",
                client = Client(
                    id = client.id.value,
                    createdAt = client.createdAt
                ),
                createdAt = LocalDate.now()
            )
        )

        val fetched = repository.get(saved.id!!)

        assertEquals("PL1234567890", fetched?.bankAccount)
        assertEquals(client.id.value, fetched?.client?.id)
    }

    @Test
    fun `should update bank account`() = runBlocking {
        val client = createClient("Update Test")
        val original = repository.save(
            ClientBankAccount(
                bankAccount = "OLD_IBAN",
                client = Client(id = client.id.value, createdAt = client.createdAt),
                createdAt = LocalDate.now()
            )
        )

        val updated = repository.update(original.id!!, original.copy(bankAccount = "NEW_IBAN"))

        assertEquals("NEW_IBAN", updated?.bankAccount)
    }

    @Test
    fun `should delete bank account`() = runBlocking {
        val client = createClient("Delete Test")
        val saved = repository.save(
            ClientBankAccount(
                bankAccount = "PL_DELETE",
                client = Client(id = client.id.value, createdAt = client.createdAt),
                createdAt = LocalDate.now()
            )
        )

        val deleted = repository.delete(saved.id!!)

        assertTrue(deleted)
        Assertions.assertNull(repository.get(saved.id!!))
    }

    @Test
    fun `should get all accounts for a client`() = runBlocking {
        val client = createClient("Multi-Account")

        repeat(3) {
            repository.save(
                ClientBankAccount(
                    bankAccount = "PL000$it",
                    client = Client(id = client.id.value, createdAt = client.createdAt),
                    createdAt = LocalDate.now()
                )
            )
        }

        val all = repository.getAllForClient(client.id.value)

        assertEquals(3, all.size)
        assertTrue(all.all { it.client?.id == client.id.value })
    }
}