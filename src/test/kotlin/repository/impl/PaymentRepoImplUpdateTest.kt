package com.kontenery.repository.impl

import com.kontenery.data.Client
import com.kontenery.data.Payment
import com.kontenery.data.invoice.Invoice
import com.kontenery.data.utils.SellerAccount
import com.kontenery.repository.entity.AddressTable
import com.kontenery.repository.entity.ClientBankAccountTable
import com.kontenery.repository.entity.ClientCompanyDataTable
import com.kontenery.repository.entity.ClientEntity
import com.kontenery.repository.entity.ClientPersonalDataTable
import com.kontenery.repository.entity.ClientTable
import com.kontenery.repository.entity.PaymentEntity
import com.kontenery.repository.entity.PaymentInvoices
import com.kontenery.repository.entity.PaymentTable
import com.kontenery.repository.entity.invoice.InvoiceEntity
import com.kontenery.repository.entity.invoice.InvoiceTable
import com.kontenery.repository.entity.invoice.Positions
import com.kontenery.repository.entity.invoice.SubjectEntity
import com.kontenery.repository.entity.invoice.Subjects
import com.kontenery.repository.entity.suspendTransaction
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentRepoImplUpdateTest {

    private val repository = PaymentRepoImpl()

    @BeforeAll
    fun setupDb() {
        Database.connect(
            "jdbc:h2:mem:payment_repo_update;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
        )
        transaction {
            SchemaUtils.create(
                AddressTable,
                ClientPersonalDataTable,
                ClientCompanyDataTable,
                ClientTable,
                ClientBankAccountTable,
                Subjects,
                InvoiceTable,
                Positions,
                PaymentTable,
                PaymentInvoices,
            )
        }
    }

    @AfterAll
    fun tearDownDb() {
        transaction {
            SchemaUtils.drop(
                PaymentInvoices,
                PaymentTable,
                Positions,
                InvoiceTable,
                Subjects,
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
            PaymentInvoices.deleteAll()
            PaymentEntity.all().forEach { it.delete() }
            InvoiceEntity.all().forEach { it.delete() }
            SubjectEntity.all().forEach { it.delete() }
            ClientEntity.all().forEach { it.delete() }
        }
    }

    @Test
    fun `updatePayment rewrites fields client and invoices`() = runBlocking {
        val clientA = insertClient()
        val clientB = insertClient()
        insertInvoice("1/1/2026")
        insertInvoice("2/1/2026")

        val created = repository.createPayment(
            Payment(
                amount = BigDecimal("100.00"),
                date = LocalDate(2026, 1, 10),
                fromClient = Client(id = clientA),
                method = "przelew",
                toAccount = SellerAccount.PRIVATE,
                fromAccount = "PL00",
                title = "stary",
                forInvoices = listOf(Invoice(invoiceNumber = "1/1/2026")),
                referenceNumber = "REF-1",
            ),
        )

        val updated = repository.updatePayment(
            created.copy(
                amount = BigDecimal("250.50"),
                date = LocalDate(2026, 2, 1),
                fromClient = Client(id = clientB),
                method = "gotówka",
                toAccount = SellerAccount.BUSINESS,
                fromAccount = "PL11",
                title = "nowy",
                forInvoices = listOf(Invoice(invoiceNumber = "2/1/2026")),
                referenceNumber = "REF-2",
            ),
        )

        assertEquals(created.id, updated.id)
        assertEquals(BigDecimal("250.50"), updated.amount)
        assertEquals(LocalDate(2026, 2, 1), updated.date)
        assertEquals(clientB, updated.fromClient?.id)
        assertEquals("gotówka", updated.method)
        assertEquals(SellerAccount.BUSINESS, updated.toAccount)
        assertEquals("PL11", updated.fromAccount)
        assertEquals("nowy", updated.title)
        assertEquals("REF-2", updated.referenceNumber)
        assertEquals(listOf("2/1/2026"), updated.forInvoices.map { it.invoiceNumber })
    }

    @Test
    fun `updatePayment clears invoices when list is empty`() = runBlocking {
        val clientId = insertClient()
        insertInvoice("1/1/2026")
        val created = repository.createPayment(
            Payment(
                amount = BigDecimal("10.00"),
                date = LocalDate(2026, 1, 10),
                fromClient = Client(id = clientId),
                forInvoices = listOf(Invoice(invoiceNumber = "1/1/2026")),
            ),
        )

        val updated = repository.updatePayment(
            created.copy(forInvoices = emptyList()),
        )

        assertTrue(updated.forInvoices.isEmpty())
    }

    @Test
    fun `updatePayment rejects missing id`() = runBlocking {
        val clientId = insertClient()
        val created = repository.createPayment(
            Payment(
                amount = BigDecimal("10.00"),
                date = LocalDate(2026, 1, 10),
                fromClient = Client(id = clientId),
            ),
        )

        var thrown = false
        try {
            repository.updatePayment(created.copy(id = null))
        } catch (e: IllegalStateException) {
            thrown = true
            assertEquals("Payment requires id", e.message)
        }
        assertTrue(thrown)
    }

    @Test
    fun `updatePayment rejects unknown id`() = runBlocking {
        val clientId = insertClient()
        val created = repository.createPayment(
            Payment(
                amount = BigDecimal("10.00"),
                date = LocalDate(2026, 1, 10),
                fromClient = Client(id = clientId),
            ),
        )

        var thrown = false
        try {
            repository.updatePayment(created.copy(id = 99999L))
        } catch (e: IllegalStateException) {
            thrown = true
            assertEquals("Payment 99999 not found", e.message)
        }
        assertTrue(thrown)
    }

    private suspend fun insertClient(): Long = suspendTransaction {
        ClientEntity.new { isActive = true }.id.value
    }

    private suspend fun insertInvoice(number: String) = suspendTransaction {
        val seller = SubjectEntity.new {
            name = "Sprzedawca"
            email = "sprzedawca@example.com"
            type = "seller"
        }
        val customer = SubjectEntity.new {
            name = "Kupujacy"
            email = "kupujacy@example.com"
            type = "customer"
        }
        InvoiceEntity.new {
            invoiceNumber = number
            invoiceTitle = "Faktura"
            invoiceDate = LocalDate(2026, 1, 1)
            this.seller = seller
            this.customer = customer
            vatAmountSum = "0"
            priceSum = "100"
            priceWithVatSum = "100"
            paymentDay = LocalDate(2026, 1, 15)
            mainAccount = "51"
        }
    }
}
