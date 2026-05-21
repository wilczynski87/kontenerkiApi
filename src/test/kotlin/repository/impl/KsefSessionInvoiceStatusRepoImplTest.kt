package com.kontenery.repository.impl

import com.kontenery.ksef.dto.KsefSessionInvoiceStatusResponse
import com.kontenery.ksef.dto.KsefStatusInfo
import com.kontenery.repository.entity.invoice.InvoiceEntity
import com.kontenery.repository.entity.invoice.InvoiceTable
import com.kontenery.repository.entity.invoice.SubjectEntity
import com.kontenery.repository.entity.invoice.Subjects
import com.kontenery.repository.entity.ksef.KsefSessionInvoiceStatusTable
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KsefSessionInvoiceStatusRepoImplTest {

    private val repo = KsefSessionInvoiceStatusRepoImpl()

    @BeforeEach
    fun setUp() {
        Database.connect("jdbc:h2:mem:ksef_status;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(InvoiceTable, Subjects, KsefSessionInvoiceStatusTable)
        }
    }

    @AfterEach
    fun tearDown() {
        transaction {
            SchemaUtils.drop(KsefSessionInvoiceStatusTable, InvoiceTable, Subjects)
        }
    }

    @Test
    fun `save and getLatestByInvoiceId persist KSeF session invoice status`() = runBlocking {
        val invoiceId = transaction {
            val seller = SubjectEntity.new {
                name = "Seller"
                type = "SELLER"
                email = "seller@test.pl"
            }
            val customer = SubjectEntity.new {
                name = "Customer"
                type = "CUSTOMER"
                email = "customer@test.pl"
            }
            val invoice = InvoiceEntity.new {
                invoiceNumber = "FV/2026/001"
                invoiceTitle = "Test"
                invoiceDate = LocalDate(2026, 1, 15)
                this.seller = seller
                this.customer = customer
                vatAmountSum = "23.00"
                priceSum = "100.00"
                priceWithVatSum = "123.00"
                paymentDay = LocalDate(2026, 1, 29)
                mainAccount = "50 0000 0000 0000 0000 0000 0001"
            }
            invoice.id.value
        }

        val response = KsefSessionInvoiceStatusResponse(
            referenceNumber = "inv-ref-1",
            invoiceNumber = "FV/2026/001",
            ksefNumber = "KSeF-123",
            status = KsefStatusInfo(code = 200, description = "OK"),
            permanentStorageDate = "2026-01-15T10:00:00Z",
        )

        val saved = repo.save(invoiceId, response)
        val latest = repo.getLatestByInvoiceId(invoiceId)

        assertNotNull(saved.id)
        assertEquals(invoiceId, saved.invoiceId)
        assertEquals("KSeF-123", saved.ksefNumber)
        assertEquals(200, saved.statusCode)
        assertEquals(latest, saved)
        assertEquals(1, repo.getAllByInvoiceId(invoiceId).size)
    }
}
