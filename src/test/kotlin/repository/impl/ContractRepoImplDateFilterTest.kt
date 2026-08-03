package com.kontenery.repository.impl

import com.kontenery.repository.entity.ClientBankAccountTable
import com.kontenery.repository.entity.ClientEntity
import com.kontenery.repository.entity.ClientTable
import com.kontenery.repository.entity.ContractEntity
import com.kontenery.repository.entity.ContractTable
import com.kontenery.repository.entity.DepositTable
import com.kontenery.repository.entity.ProductEntity
import com.kontenery.repository.entity.ProductTable
import com.kontenery.repository.entity.ProductType
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ContractRepoImplDateFilterTest {

    private lateinit var repo: ContractRepoImpl

    @BeforeEach
    fun setUp() {
        Database.connect(
            url = "jdbc:h2:mem:contract_date_filter;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;",
            driver = "org.h2.Driver",
        )
        transaction {
            SchemaUtils.create(
                ClientTable,
                ClientBankAccountTable,
                ProductTable,
                DepositTable,
                ContractTable,
            )
        }
        repo = ContractRepoImpl()
    }

    @AfterEach
    fun tearDown() {
        transaction {
            SchemaUtils.drop(
                ContractTable,
                DepositTable,
                ProductTable,
                ClientBankAccountTable,
                ClientTable,
            )
        }
    }

    @Test
    fun `null startDate and endDate match billing period`() = runBlocking {
        val clientId = transaction {
            val client = ClientEntity.new { isActive = true }
            ContractEntity.new {
                this.client = client
                startDate = null
                endDate = null
                netPrice = BigDecimal("850.00")
                vatRate = BigDecimal("23.00")
            }
            client.id.value
        }

        val found = repo.findByClientId(
            clientId,
            LocalDate(2026, 8, 1),
            LocalDate(2026, 8, 31),
        )

        assertEquals(1, found.size)
        assertEquals(clientId, found.single().client?.id)
    }

    @Test
    fun `contract started after period start is excluded`() = runBlocking {
        val clientId = transaction {
            val client = ClientEntity.new { isActive = true }
            ContractEntity.new {
                this.client = client
                startDate = LocalDate(2026, 8, 15)
                endDate = null
                netPrice = BigDecimal("100.00")
                vatRate = BigDecimal("23.00")
            }
            client.id.value
        }

        val found = repo.findByClientId(
            clientId,
            LocalDate(2026, 8, 1),
            LocalDate(2026, 8, 31),
        )

        assertEquals(0, found.size)
    }

    @Test
    fun `contract with start on or before period start is included`() = runBlocking {
        val clientId = transaction {
            val client = ClientEntity.new { isActive = true }
            val product = ProductEntity.new {
                name = "Kontener"
                type = ProductType.CONTAINER
            }
            ContractEntity.new {
                this.client = client
                this.product = product
                startDate = LocalDate(2026, 7, 1)
                endDate = null
                netPrice = BigDecimal("100.00")
                vatRate = BigDecimal("23.00")
            }
            client.id.value
        }

        val found = repo.findByClientId(
            clientId,
            LocalDate(2026, 8, 1),
            LocalDate(2026, 8, 31),
        )

        assertEquals(1, found.size)
    }
}
